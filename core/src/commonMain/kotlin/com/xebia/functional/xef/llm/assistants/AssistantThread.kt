package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.parMapNotNull
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.addMetrics
import com.xebia.functional.xef.metrics.Metric
import io.github.nomisrev.openapi.*
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
  private val api: OpenAPI = OpenAI(config)
) {

  suspend fun delete(): Boolean =
    api.threads.deleteThread(threadId = threadId, configure = ::defaultConfig).deleted

  suspend fun modify(request: ModifyThreadRequest): AssistantThread =
    AssistantThread(api.threads.modifyThread(threadId, request, configure = ::defaultConfig).id)

  suspend fun createMessage(message: MessageWithFiles): MessageObject =
    createMessage(
      request =
        CreateMessageRequest(
          role = CreateMessageRequest.Role.User,
          content = message.content,
          fileIds = message.fileIds
        ),
    )

  suspend fun createMessage(content: String): MessageObject =
    createMessage(CreateMessageRequest(role = CreateMessageRequest.Role.User, content = content))

  suspend fun createMessage(request: CreateMessageRequest): MessageObject =
    api.threads.messages.createMessage(threadId, request, configure = ::defaultConfig)

  suspend fun getMessage(messageId: String): MessageObject =
    api.threads.messages.getMessage(threadId, messageId, configure = ::defaultConfig)

  data class ThreadMessagesFilter(
    val limit: Int? = 20,
    val order: Threads.Messages.ListMessagesOrder = Threads.Messages.ListMessagesOrder.Desc,
    val after: String? = null,
    val before: String? = null
  )

  suspend fun listMessages(
    filter: ThreadMessagesFilter = ThreadMessagesFilter()
  ): List<MessageObject> =
    api.threads.messages
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
    api.threads.runs.createRun(threadId, request, configure = ::defaultConfig).addMetrics(metric)

  fun createRunStream(assistant: Assistant, request: CreateRunRequest): Flow<RunDelta> = flow {
    api.threads.runs
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
      val run =
        metric.assistantToolOutputsRun(event.run.id) {
          api.threads.runs.submitToolOutputs
            .submitToolOuputsToRunStream(
              threadId = threadId,
              runId = event.run.id,
              body = toolOutputsRequest,
              configure = ::defaultConfig
            )
            .collect {
              val delta = RunDelta.fromServerSentEvent(it)
              if (delta is RunDelta.RunStepCompleted) {
                flowCollector.emit(RunDelta.RunSubmitToolOutputs(toolOutputsRequest))
              }
              flowCollector.emit(delta)
            }
          val run = getRun(event.run.id)
          val finalEvent =
            when (run.status) {
              RunObject.Status.Queued -> RunDelta.RunQueued(run)
              RunObject.Status.InProgress -> RunDelta.RunInProgress(run)
              RunObject.Status.RequiresAction -> RunDelta.RunRequiresAction(run)
              RunObject.Status.Cancelling -> RunDelta.RunCancelling(run)
              RunObject.Status.Cancelled -> RunDelta.RunCancelled(run)
              RunObject.Status.Failed -> RunDelta.RunFailed(run)
              RunObject.Status.Completed -> RunDelta.RunCompleted(run)
              RunObject.Status.Expired -> RunDelta.RunExpired(run)
            }
          flowCollector.emit(finalEvent)
          run
        }

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
      return if (functionName != null && functionArguments != null) {
        val result = assistant.getToolRegistered(functionName, functionArguments)
        val callId = toolCall.id
        if (callId != null) {
          callId to result
        } else null
      } else null
    } catch (e: Throwable) {
      toolCall.id to
        Assistant.Companion.ToolOutput(
          schema = JsonObject(emptyMap()),
          result = JsonObject(mapOf("error" to JsonPrimitive(e.message ?: "Unknown error")))
        )
    }
  }

  suspend fun getRun(runId: String): RunObject =
    api.threads.runs.getRun(threadId, runId, configure = ::defaultConfig)

  fun run(assistant: Assistant): Flow<RunDelta> =
    createRunStream(assistant, CreateRunRequest(assistantId = assistant.assistantId))

  suspend fun cancelRun(runId: String): RunObject =
    api.threads.runs.cancel.cancelRun(threadId, runId, configure = ::defaultConfig)

  suspend fun runSteps(runId: String): List<RunStepObject> =
    api.threads.runs.steps.listRunSteps(threadId, runId, configure = ::defaultConfig).data

  private fun RunStepObject.StepDetails.toolCalls(): List<RunStepDetailsToolCallsObject.ToolCalls> =
    when (val step = this) {
      is RunStepObject.StepDetails.CaseRunStepDetailsMessageCreationObject -> emptyList()
      is RunStepObject.StepDetails.CaseRunStepDetailsToolCallsObject -> step.value.toolCalls
    }

  companion object {

    /** Support for OpenAI-Beta: assistants=v1 */
    fun defaultConfig(httpRequestBuilder: HttpRequestBuilder): Unit {
      httpRequestBuilder.header("OpenAI-Beta", "assistants=v1")
    }

    @JvmName("createWithMessagesAndFiles")
    suspend operator fun invoke(
      messages: List<MessageWithFiles>,
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: OpenAPI = OpenAI(config)
    ): AssistantThread =
      AssistantThread(
        threadId =
          api.threads
            .createThread(
              body =
                CreateThreadRequest(
                  messages.map {
                    CreateMessageRequest(
                      role = CreateMessageRequest.Role.User,
                      content = it.content,
                      fileIds = it.fileIds
                    )
                  },
                  metadata
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
      api: OpenAPI = OpenAI(config)
    ): AssistantThread =
      AssistantThread(
        api.threads
          .createThread(
            body =
              CreateThreadRequest(
                messages.map {
                  CreateMessageRequest(role = CreateMessageRequest.Role.User, content = it)
                },
                metadata
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
      api: OpenAPI = OpenAI(config)
    ): AssistantThread =
      AssistantThread(
        api.threads
          .createThread(CreateThreadRequest(messages, metadata), configure = ::defaultConfig)
          .id,
        metric,
        config,
        api
      )

    suspend operator fun invoke(
      request: CreateThreadRequest,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: OpenAPI = OpenAI(config)
    ): AssistantThread =
      AssistantThread(
        api.threads.createThread(request, configure = ::defaultConfig).id,
        metric,
        config,
        api
      )

    suspend operator fun invoke(
      request: CreateThreadAndRunRequest,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: OpenAPI = OpenAI(config)
    ): AssistantThread =
      AssistantThread(
        api.threads.runs.createThreadAndRun(request, configure = ::defaultConfig).id,
        metric,
        config,
        api
      )
  }
}
