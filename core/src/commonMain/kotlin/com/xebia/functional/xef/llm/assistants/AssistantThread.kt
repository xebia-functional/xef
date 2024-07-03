package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.parMapNotNull
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.addMetrics
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.openapi.*
import com.xebia.functional.xef.openapi.Threads.Messages.ListMessagesOrder
import io.ktor.client.request.*
import kotlin.jvm.JvmName
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class AssistantThread(
  val threadId: String,
  val metric: Metric = Metric.EMPTY,
  private val config: Config = Config(),
  private val api: Threads = OpenAI(config).threads
) {

  suspend fun delete(): Boolean =
    api.deleteThread(threadId = threadId, configure = ::defaultConfig).deleted

  suspend fun modify(request: ModifyThreadRequest): AssistantThread =
    AssistantThread(api.modifyThread(threadId, request, configure = ::defaultConfig).id)

  suspend fun createMessage(message: MessageWithFiles): MessageObject =
    createMessage(
      request =
        CreateMessageRequest(
          role = CreateMessageRequest.Role.User,
          content = CreateMessageRequest.Content.CaseString(message.content),
          attachments = message.fileIds.map { CreateMessageRequest.Attachments(fileId = it) }
        ),
    )

  suspend fun createMessage(content: String): MessageObject =
    createMessage(
      CreateMessageRequest(
        role = CreateMessageRequest.Role.User,
        content = CreateMessageRequest.Content.CaseString(content)
      )
    )

  suspend fun createMessage(request: CreateMessageRequest): MessageObject =
    api.messages.createMessage(threadId, request, configure = ::defaultConfig)

  suspend fun getMessage(messageId: String): MessageObject =
    api.messages.getMessage(threadId, messageId, configure = ::defaultConfig)

  data class ThreadMessagesFilter(
    val limit: Int? = 20,
    val order: ListMessagesOrder? = ListMessagesOrder.Desc,
    val after: String? = null,
    val before: String? = null
  )

  suspend fun listMessages(
    filter: ThreadMessagesFilter = ThreadMessagesFilter()
  ): List<MessageObject> =
    api.messages
      .listMessages(
        threadId = threadId,
        limit = filter.limit,
        order = filter.order,
        after = filter.after,
        before = filter.before,
        configure = ::defaultConfig
      )
      .data

  suspend fun createRun(assistant: Assistant): RunObject =
    createRun(CreateRunRequest(assistantId = assistant.assistantId))

  suspend fun createRun(request: CreateRunRequest): RunObject =
    api.runs
      .createRun(threadId, request, configure = ::defaultConfig)
      .addMetrics(metric, "RunCreated")

  fun createRunStream(assistant: Assistant, request: CreateRunRequest): Flow<RunDelta> = flow {
    api.runs
      .createRunStream(threadId, request, configure = ::defaultConfig)
      .map { RunDelta.fromServerSentEvent(it) }
      .map { it.addMetrics(metric) }
      .collect { event ->
        when (event) {
          // submit tool outputs and join streams
          is RunDelta.RunRequiresAction -> {
            takeRequiredAction(1, event, this@AssistantThread, assistant, this)
          }
          // previous to submitting tool outputs we let all events pass through the outer flow
          else -> {
            emit(event)
          }
        }
      }
  }

  private suspend fun takeRequiredAction(
    depth: Int,
    event: RunDelta.RunRequiresAction,
    assistantThread: AssistantThread,
    assistant: Assistant,
    flowCollector: FlowCollector<RunDelta>
  ) {
    if (
      event.run.status == RunObject.Status.RequiresAction &&
        event.run.requiredAction?.type == RunObject.RequiredAction.Type.SubmitToolOutputs
    ) {
      val calls = event.run.requiredAction?.submitToolOutputs?.toolCalls.orEmpty()
      val callsResult: List<Pair<String, Assistant.Companion.ToolOutput>> =
        calls.parMapNotNull { toolCall -> assistantThread.executeToolCall(toolCall, assistant) }
      val results: Map<String, Assistant.Companion.ToolOutput> = callsResult.toMap()
      val toolOutputsRequest =
        SubmitToolOutputsRunRequest(
          toolOutputs =
            results.map { (toolCallId, result) ->
              SubmitToolOutputsRunRequest.ToolOutputs(
                toolCallId = toolCallId,
                output = Json.encodeToString(Assistant.Companion.ToolOutput.serializer(), result)
              )
            }
        )

      api.runs.submitToolOutputs
        .submitToolOuputsToRunStream(
          threadId = threadId,
          runId = event.run.id,
          body = toolOutputsRequest,
          configure = ::defaultConfig
        )
        .collect {
          val delta = RunDelta.fromServerSentEvent(it)

          delta.launchMetricsIfNecessary()

          if (delta is RunDelta.RunStepCompleted) {
            flowCollector.emit(RunDelta.RunSubmitToolOutputs(toolOutputsRequest))
          }
          flowCollector.emit(delta)
        }

      val run = getRun(event.run.id)
      val finalEvent =
        when (run.status) {
          RunObject.Status.Queued -> Pair(RunDelta.RunQueued(run), "RunQueued")
          RunObject.Status.InProgress -> Pair(RunDelta.RunInProgress(run), "RunInProgress")
          RunObject.Status.RequiresAction ->
            Pair(RunDelta.RunRequiresAction(run), "RunRequiresAction")
          RunObject.Status.Cancelling -> Pair(RunDelta.RunCancelling(run), "RunCancelling")
          RunObject.Status.Cancelled -> Pair(RunDelta.RunCancelled(run), "RunCancelled")
          RunObject.Status.Failed -> Pair(RunDelta.RunFailed(run), "RunFailed")
          RunObject.Status.Completed -> Pair(RunDelta.RunCompleted(run), "RunCompleted")
          RunObject.Status.Expired -> Pair(RunDelta.RunExpired(run), "RunExpired")
          RunObject.Status.Incomplete -> Pair(RunDelta.RunIncomplete(run), "RunIncomplete")
        }
      flowCollector.emit(finalEvent.first)
      metric.assistantCreateRun(run, finalEvent.second)

      if (run.status == RunObject.Status.RequiresAction) {
        takeRequiredAction(
          depth + 1,
          RunDelta.RunRequiresAction(run),
          assistantThread,
          assistant,
          flowCollector
        )
      }
    }
  }

  private suspend fun executeToolCall(
    toolCall: RunToolCallObject,
    assistant: Assistant
  ): Pair<String, Assistant.Companion.ToolOutput>? {
    return try {
      val function = toolCall.function
      val functionName = function.name
      val functionArguments = function.arguments
      val result = assistant.getToolRegistered(functionName, functionArguments)
      val callId = toolCall.id
      return callId to result
    } catch (e: Throwable) {
      toolCall.id to
        Assistant.Companion.ToolOutput(
          schema = JsonObject(emptyMap()),
          result = JsonObject(mapOf("error" to JsonPrimitive(e.message ?: "Unknown error")))
        )
    }
  }

  suspend fun getRun(runId: String): RunObject =
    api.runs.getRun(threadId, runId, configure = ::defaultConfig)

  fun run(assistant: Assistant): Flow<RunDelta> =
    createRunStream(assistant, CreateRunRequest(assistantId = assistant.assistantId))

  suspend fun cancelRun(runId: String): RunObject =
    api.runs.cancel.cancelRun(threadId, runId, configure = ::defaultConfig)

  suspend fun runSteps(runId: String): List<RunStepObject> =
    api.runs.steps.listRunSteps(threadId, runId, configure = ::defaultConfig).data

  private fun RunStepObject.StepDetails.toolCalls(): List<RunStepDetailsToolCallsObject.ToolCalls> =
    when (val step = this) {
      is RunStepObject.StepDetails.CaseRunStepDetailsMessageCreationObject -> emptyList()
      is RunStepObject.StepDetails.CaseRunStepDetailsToolCallsObject -> step.value.toolCalls
    }

  private suspend fun RunDelta.launchMetricsIfNecessary() {
    launchRunMetricsIfNecessary()
    launchRunStepsMetricsIfNecessary()
    launchMessageMetricsIfNecessary()
  }

  private suspend fun RunDelta.launchRunMetricsIfNecessary() {
    when (this) {
      is RunDelta.RunCreated -> Pair(run, "RunCreated")
      is RunDelta.RunQueued -> Pair(run, "RunQueued")
      is RunDelta.RunFailed -> Pair(run, "RunFailed")
      is RunDelta.RunCancelled -> Pair(run, "RunCancelled")
      is RunDelta.RunCancelling -> Pair(run, "RunCancelling")
      is RunDelta.RunExpired -> Pair(run, "RunExpired")
      is RunDelta.RunInProgress -> Pair(run, "RunInProgress")
      is RunDelta.RunIncomplete -> Pair(run, "RunIncomplete")
      else -> null
    }?.let { metric.assistantCreateRun(it.first, it.second) }
  }

  private suspend fun RunDelta.launchRunStepsMetricsIfNecessary() {
    when (this) {
      is RunDelta.RunStepCreated -> Pair(runStep, "RunStepCreated")
      is RunDelta.RunStepInProgress -> Pair(runStep, "RunStepInProgress")
      is RunDelta.RunStepCompleted -> Pair(runStep, "RunStepCompleted")
      is RunDelta.RunStepFailed -> Pair(runStep, "RunStepFailed")
      is RunDelta.RunStepCancelled -> Pair(runStep, "RunStepCancelled")
      is RunDelta.RunStepExpired -> Pair(runStep, "RunStepExpired")
      else -> null
    }?.let { metric.assistantCreateRunStep(it.first, it.second) }
  }

  private suspend fun RunDelta.launchMessageMetricsIfNecessary() {
    when (this) {
      is RunDelta.MessageCreated -> Pair(message, "MessageCreated")
      is RunDelta.MessageInProgress -> Pair(message, "MessageInProgress")
      is RunDelta.MessageIncomplete -> Pair(message, "MessageIncomplete")
      is RunDelta.MessageCompleted -> Pair(message, "MessageCompleted")
      else -> null
    }?.let { metric.assistantCreatedMessage(it.first, it.second) }
  }

  companion object {

    /** Support for OpenAI-Beta: assistants=v2 */
    fun defaultConfig(httpRequestBuilder: HttpRequestBuilder): Unit {
      httpRequestBuilder.header("OpenAI-Beta", "assistants=v2")
    }

    @JvmName("createWithMessagesAndFiles")
    suspend operator fun invoke(
      messages: List<MessageWithFiles>,
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: Threads = OpenAI(config).threads
    ): AssistantThread =
      AssistantThread(
        threadId =
          api
            .createThread(
              body =
                CreateThreadRequest(
                  messages =
                    messages.map {
                      CreateMessageRequest(
                        role = CreateMessageRequest.Role.User,
                        content = CreateMessageRequest.Content.CaseString(it.content),
                        attachments =
                          it.fileIds.map { CreateMessageRequest.Attachments(fileId = it) }
                      )
                    },
                  metadata = metadata
                ),
              configure = ::defaultConfig
            )
            .id,
        metric = metric,
        config = config,
        api = api
      )

    @JvmName("createWithMessages")
    suspend operator fun invoke(
      messages: List<String>,
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: Threads = OpenAI(config).threads
    ): AssistantThread =
      AssistantThread(
        api
          .createThread(
            body =
              CreateThreadRequest(
                messages =
                  messages.map {
                    CreateMessageRequest(
                      role = CreateMessageRequest.Role.User,
                      content = CreateMessageRequest.Content.CaseString(it)
                    )
                  },
                metadata = metadata
              ),
            configure = ::defaultConfig
          )
          .id,
        metric,
        config,
        api
      )

    @JvmName("createWithRequests")
    suspend operator fun invoke(
      messages: List<CreateMessageRequest> = emptyList(),
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: Threads = OpenAI(config).threads
    ): AssistantThread =
      AssistantThread(
        api
          .createThread(
            CreateThreadRequest(messages = messages, metadata = metadata),
            configure = ::defaultConfig
          )
          .id,
        metric,
        config,
        api
      )

    suspend operator fun invoke(
      request: CreateThreadRequest,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: Threads = OpenAI(config).threads
    ): AssistantThread =
      AssistantThread(
        api.createThread(request, configure = ::defaultConfig).id,
        metric,
        config,
        api
      )

    suspend operator fun invoke(
      request: CreateThreadAndRunRequest,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: Threads = OpenAI(config).threads
    ): AssistantThread =
      AssistantThread(
        api.runs.createThreadAndRun(request, configure = ::defaultConfig).id,
        metric,
        config,
        api
      )
  }
}
