package com.xebia.functional.xef.llm.assistants

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.apis.AssistantsApi
import com.xebia.functional.openai.apis.AssistantsApi.OrderListMessages
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.assistant.RunStepDetailsMessageCreationObject
import com.xebia.functional.openai.models.ext.assistant.RunStepDetailsToolCallsObject
import com.xebia.functional.openai.models.ext.assistant.RunStepObjectStepDetails
import com.xebia.functional.xef.llm.addMetrics
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.metrics.Metric
import kotlin.jvm.JvmName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject

class AssistantThread(
  val threadId: String,
  val metric: Metric = Metric.EMPTY,
  private val api: AssistantsApi = fromEnvironment(::AssistantsApi)
) {

  suspend fun delete(): Boolean = api.deleteThread(threadId).body().deleted

  suspend fun modify(request: ModifyThreadRequest): AssistantThread =
    AssistantThread(api.modifyThread(threadId, request).body().id)

  suspend fun createMessage(message: MessageWithFiles): MessageObject =
    createMessage(
      CreateMessageRequest(
        role = CreateMessageRequest.Role.user,
        content = message.content,
        fileIds = message.fileIds
      )
    )

  suspend fun createMessage(content: String): MessageObject =
    createMessage(CreateMessageRequest(role = CreateMessageRequest.Role.user, content = content))

  suspend fun createMessage(request: CreateMessageRequest): MessageObject =
    api.createMessage(threadId, request).body()

  suspend fun getMessage(messageId: String): MessageObject =
    api.getMessage(threadId, messageId).body()

  data class ThreadMessagesFilter(
    val limit: Int? = 20,
    val order: OrderListMessages? = OrderListMessages.desc,
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
        before = filter.before
      )
      .body()
      .data

  suspend fun createRun(request: CreateRunRequest): RunObject =
    api.createRun(threadId, request).body().addMetrics(metric)

  suspend fun getRun(runId: String): RunObject = api.getRun(threadId, runId).body()

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

  suspend fun cancelRun(runId: String): RunObject = api.cancelRun(threadId, runId).body()

  suspend fun runSteps(runId: String): List<RunStepObject> =
    api.listRunSteps(threadId, runId).body().data

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
        val content = message.content.filterNot { it.text?.value?.isBlank() ?: true }
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
    when (this) {
      is RunStepDetailsMessageCreationObject -> listOf()
      is RunStepDetailsToolCallsObject -> toolCalls
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
            .filter { it.function != null }
            .parMap { toolCall ->
              val function = toolCall.function!!
              val result = assistant.getToolRegistered(function.name, function.arguments)
              toolCall.id to result
            }
            .toMap()

        metric.assistantToolOutputsRun(runId) {
          api
            .submitToolOuputsToRun(
              threadId = threadId,
              runId = runId,
              submitToolOutputsRunRequest =
                SubmitToolOutputsRunRequest(
                  toolOutputs =
                    results.map { (toolCallId, result) ->
                      SubmitToolOutputsRunRequestToolOutputsInner(
                        toolCallId = toolCallId,
                        output =
                          ApiClient.JSON_DEFAULT.encodeToString(
                            Assistant.Companion.ToolOutput.serializer(),
                            result
                          )
                      )
                    }
                )
            )
            .body()
        }
      }
    }
  }

  companion object {

    @JvmName("createWithMessagesAndFiles")
    suspend operator fun invoke(
      messages: List<MessageWithFiles>,
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      api: AssistantsApi = fromEnvironment(::AssistantsApi)
    ): AssistantThread =
      AssistantThread(
        api
          .createThread(
            CreateThreadRequest(
              messages.map {
                CreateMessageRequest(
                  role = CreateMessageRequest.Role.user,
                  content = it.content,
                  fileIds = it.fileIds
                )
              },
              metadata
            )
          )
          .body()
          .id,
        metric,
        api
      )

    @JvmName("createWithMessages")
    suspend operator fun invoke(
      messages: List<String>,
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      api: AssistantsApi = fromEnvironment(::AssistantsApi)
    ): AssistantThread =
      AssistantThread(
        api
          .createThread(
            CreateThreadRequest(
              messages.map {
                CreateMessageRequest(role = CreateMessageRequest.Role.user, content = it)
              },
              metadata
            )
          )
          .body()
          .id,
        metric,
        api
      )

    @JvmName("createWithRequests")
    suspend operator fun invoke(
      messages: List<CreateMessageRequest> = emptyList(),
      metadata: JsonObject? = null,
      metric: Metric = Metric.EMPTY,
      api: AssistantsApi = fromEnvironment(::AssistantsApi)
    ): AssistantThread =
      AssistantThread(
        api.createThread(CreateThreadRequest(messages, metadata)).body().id,
        metric,
        api
      )

    suspend operator fun invoke(
      request: CreateThreadRequest,
      metric: Metric = Metric.EMPTY,
      api: AssistantsApi = fromEnvironment(::AssistantsApi)
    ): AssistantThread = AssistantThread(api.createThread(request).body().id, metric, api)

    suspend operator fun invoke(
      request: CreateThreadAndRunRequest,
      metric: Metric = Metric.EMPTY,
      api: AssistantsApi = fromEnvironment(::AssistantsApi)
    ): AssistantThread = AssistantThread(api.createThreadAndRun(request).body().id, metric, api)
  }
}
