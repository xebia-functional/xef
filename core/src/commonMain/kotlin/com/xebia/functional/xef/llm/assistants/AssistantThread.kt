package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.addMetrics
import com.xebia.functional.xef.metrics.Metric
import io.ktor.client.request.*
import kotlin.jvm.JvmName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class AssistantThread(
  val threadId: String,
  val metric: Metric = Metric.EMPTY,
  private val config: Config = Config(),
  private val api: Assistants = OpenAI(config).assistants
) {

  suspend fun delete(): Boolean =
    api.deleteThread(threadId = threadId, configure = ::defaultConfig).deleted

  suspend fun modify(request: ModifyThreadRequest): AssistantThread =
    AssistantThread(api.modifyThread(threadId, request, configure = ::defaultConfig).id)

  suspend fun createMessage(message: MessageWithFiles): MessageObject =
    createMessage(
      request =
        CreateMessageRequest(
          role = CreateMessageRequest.Role.user,
          content = message.content,
          fileIds = message.fileIds
        ),
    )

  suspend fun createMessage(content: String): MessageObject =
    createMessage(CreateMessageRequest(role = CreateMessageRequest.Role.user, content = content))

  suspend fun createMessage(request: CreateMessageRequest): MessageObject =
    api.createMessage(threadId, request, configure = ::defaultConfig)

  suspend fun getMessage(messageId: String): MessageObject =
    api.getMessage(threadId, messageId, configure = ::defaultConfig)

  data class ThreadMessagesFilter(
    val limit: Int? = 20,
    val order: Assistants.OrderListMessages? = Assistants.OrderListMessages.desc,
    val after: String? = null,
    val before: String? = null
  )

  suspend fun listMessages(
    filter: ThreadMessagesFilter = ThreadMessagesFilter()
  ): List<MessageObject> =
    api
      .listMessages(
        threadId = threadId,
        limit = filter.limit,
        order = filter.order,
        after = filter.after,
        before = filter.before,
        configure = ::defaultConfig
      )
      .data

  suspend fun createRun(request: CreateRunRequest): RunObject =
    api.createRun(threadId, request, configure = ::defaultConfig).addMetrics(metric)

  suspend fun getRun(runId: String): RunObject =
    api.getRun(threadId, runId, configure = ::defaultConfig)

  suspend fun createRun(assistant: Assistant): RunObject =
    createRun(CreateRunRequest(assistantId = assistant.assistantId))

  suspend fun run(
    assistant: Assistant,
    filter: ThreadMessagesFilter = ThreadMessagesFilter()
  ): Flow<RunDelta> {
    val run = createRun(assistant)
    return awaitRun(assistant, run.id, filter)
  }

  suspend fun run(
    assistant: Assistant,
    request: CreateRunRequest,
    filter: ThreadMessagesFilter = ThreadMessagesFilter()
  ): Flow<RunDelta> {
    val run = createRun(request)
    return awaitRun(assistant, run.id, filter)
  }

  suspend fun cancelRun(runId: String): RunObject =
    api.cancelRun(threadId, runId, configure = ::defaultConfig)

  suspend fun runSteps(runId: String): List<RunStepObject> =
    api.listRunSteps(threadId, runId, configure = ::defaultConfig).data

  sealed class RunDelta {
    data class ReceivedMessage(val message: MessageObject) : RunDelta()

    data class Run(val message: RunObject) : RunDelta()

    data class Step(val runStep: RunStepObject) : RunDelta()
  }

  private fun awaitRun(
    assistant: Assistant,
    runId: String,
    filter: ThreadMessagesFilter
  ): Flow<RunDelta> = flow {
    val stepCache = mutableSetOf<RunStepObject>() // CacheTool
    val messagesCache = mutableSetOf<MessageObject>()
    val runCache = mutableSetOf<RunObject>()
    try {
      var run = checkRun(runId = runId, cache = runCache)
      while (run.status != RunObject.Status.completed) {
        checkSteps(assistant = assistant, runId = runId, cache = stepCache)
        delay(500) // To avoid excessive calls to OpenAI
        checkMessages(runId = runId, cache = messagesCache, filter = filter)
        delay(500) // To avoid excessive calls to OpenAI
        run = checkRun(runId = runId, cache = runCache)
      }
    } catch (e: Exception) {
      emit(
        RunDelta.Run(
          RunObject(
            id = runId,
            `object` = RunObject.Object.thread_run,
            createdAt = 0,
            threadId = threadId,
            assistantId = assistant.assistantId,
            status = RunObject.Status.failed,
            lastError =
              RunObjectLastError(
                code = RunObjectLastError.Code.server_error,
                message = e.message ?: "Unknown error"
              ),
            startedAt = null,
            cancelledAt = null,
            failedAt = null,
            completedAt = null,
            model = "",
            instructions = "",
            tools = emptyList(),
            fileIds = emptyList(),
            metadata = null,
            usage = null
          )
        )
      )
    } finally {
      checkMessages(runId = runId, cache = messagesCache, filter = filter)
    }
  }

  private suspend fun FlowCollector<RunDelta>.checkRun(
    runId: String,
    cache: MutableSet<RunObject>
  ): RunObject {
    val run = metric.assistantCreateRun(runId) { getRun(runId) }
    if (run !in cache) {
      cache.add(run)
      emit(RunDelta.Run(run))
    }
    return run
  }

  private suspend fun FlowCollector<RunDelta>.checkMessages(
    runId: String,
    cache: MutableSet<MessageObject>,
    filter: ThreadMessagesFilter,
  ) {
    metric.assistantCreatedMessage(runId) {
      val messages = mutableListOf<MessageObject>()
      val updatedAndNewMessages = listMessages(filter).filterNot { it in cache }
      updatedAndNewMessages.forEach { message ->
        val content =
          message.content.filterNot {
            when (it) {
              is MessageObjectContentInner.First -> false
              is MessageObjectContentInner.Second -> it.value.text.value.isBlank()
            }
          }
        if (content.isNotEmpty() && message !in cache) {
          cache.add(message)
          messages.add(message)
          emit(RunDelta.ReceivedMessage(message))
        }
      }
      messages
    }
  }

  private fun RunStepObjectStepDetails.toolCalls():
    List<RunStepDetailsToolCallsObjectToolCallsInner> =
    when (val step = this) {
      is RunStepObjectStepDetails.First -> emptyList()
      is RunStepObjectStepDetails.Second -> step.value.toolCalls
    }

  private suspend fun FlowCollector<RunDelta>.checkSteps(
    assistant: Assistant,
    runId: String,
    cache: MutableSet<RunStepObject>
  ) {

    val steps = runSteps(runId).map { metric.assistantCreateRunStep(runId) { it } }

    steps.forEach { step ->
      val calls = step.stepDetails.toolCalls()
      //          .filter {
      //            it.function != null && it.function!!.arguments.isNotBlank()
      //          }

      // We have detected that tool call in in_progress state sometimes don't have any arguments
      // and this is not valid. We need to skip this step in this case.
      val canEmitToolCalls =
        if (
          step.type == RunStepObject.Type.tool_calls &&
            step.status == RunStepObject.Status.in_progress
        )
          calls.isNotEmpty()
        else true

      val emitEvent = canEmitToolCalls && step !in cache

      if (emitEvent) {
        cache.add(step)
        emit(RunDelta.Step(step))
      }
      val run = getRun(runId)
      if (
        run.status == RunObject.Status.requires_action &&
          run.requiredAction?.type == RunObjectRequiredAction.Type.submit_tool_outputs
      ) {
        val results: Map<String, Assistant.Companion.ToolOutput> =
          calls
            .filterIsInstance<RunStepDetailsToolCallsObjectToolCallsInner.Second>()
            .parMap { toolCall ->
              val function = toolCall.value.function
              val result = assistant.getToolRegistered(function.name, function.arguments)
              toolCall.value.id to result
            }
            .toMap()

        metric.assistantToolOutputsRun(runId) {
          api.submitToolOuputsToRun(
            threadId = threadId,
            runId = runId,
            submitToolOutputsRunRequest =
              SubmitToolOutputsRunRequest(
                toolOutputs =
                  results.map { (toolCallId, result) ->
                    SubmitToolOutputsRunRequestToolOutputsInner(
                      toolCallId = toolCallId,
                      output =
                        Json.encodeToString(Assistant.Companion.ToolOutput.serializer(), result)
                    )
                  }
              ),
            configure = ::defaultConfig
          )
        }
      }
    }
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
      api: Assistants = OpenAI(config).assistants
    ): AssistantThread =
      AssistantThread(
        threadId =
          api
            .createThread(
              createThreadRequest =
                CreateThreadRequest(
                  messages.map {
                    CreateMessageRequest(
                      role = CreateMessageRequest.Role.user,
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
      api: Assistants = OpenAI(config).assistants
    ): AssistantThread =
      AssistantThread(
        api
          .createThread(
            createThreadRequest =
              CreateThreadRequest(
                messages.map {
                  CreateMessageRequest(role = CreateMessageRequest.Role.user, content = it)
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
      api: Assistants = OpenAI(config).assistants
    ): AssistantThread =
      AssistantThread(
        api.createThread(CreateThreadRequest(messages, metadata), configure = ::defaultConfig).id,
        metric,
        config,
        api
      )

    suspend operator fun invoke(
      request: CreateThreadRequest,
      metric: Metric = Metric.EMPTY,
      config: Config = Config(),
      api: Assistants = OpenAI(config).assistants
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
      api: Assistants = OpenAI(config).assistants
    ): AssistantThread =
      AssistantThread(
        api.createThreadAndRun(request, configure = ::defaultConfig).id,
        metric,
        config,
        api
      )
  }
}
