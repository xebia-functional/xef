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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

  fun createRunStream(assistant: Assistant, request: CreateRunRequest): Flow<RunDelta> = flow {
    api
      .createRunStream(threadId, request, configure = ::defaultConfig)
      .map { RunDelta.fromServerSentEvent(it) }
      .collect { event ->
        when (event) {
          // submit tool outputs and join streams
          is RunDelta.RunRequiresAction -> {
            val steps =
              runSteps(event.run.id).map { metric.assistantCreateRunStep(event.run.id) { it } }

            steps.forEach { step ->
              val calls = step.stepDetails.toolCalls()

              val run = getRun(event.run.id)
              if (
                run.status == RunObject.Status.requires_action &&
                  run.requiredAction?.type == RunObjectRequiredAction.Type.submit_tool_outputs
              ) {
                val callsResult: List<Pair<String, Assistant.Companion.ToolOutput>> =
                  calls
                    .filterIsInstance<
                      RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject
                    >()
                    .parMap { toolCall -> executeToolCall(toolCall, assistant) }
                    .filterNotNull()
                val results: Map<String, Assistant.Companion.ToolOutput> = callsResult.toMap()

                metric.assistantToolOutputsRun(event.run.id) {
                  api
                    .submitToolOuputsToRunStream(
                      threadId = threadId,
                      runId = event.run.id,
                      submitToolOutputsRunRequest =
                        SubmitToolOutputsRunRequest(
                          toolOutputs =
                            results.map { (toolCallId, result) ->
                              SubmitToolOutputsRunRequestToolOutputsInner(
                                toolCallId = toolCallId,
                                output =
                                  Json.encodeToString(
                                    Assistant.Companion.ToolOutput.serializer(),
                                    result
                                  )
                              )
                            }
                        ),
                      configure = ::defaultConfig
                    )
                    .collect { emit(RunDelta.fromServerSentEvent(it)) }
                  getRun(event.run.id)
                }
              }
            }
          }
          // previous to submitting tool outputs we let all events pass through the outer flow
          else -> emit(event)
        }
      }
  }

  private suspend fun executeToolCall(
    toolCall: RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject,
    assistant: Assistant
  ): Pair<String, Assistant.Companion.ToolOutput>? {
    val function = toolCall.value.function
    val functionName = function.name
    val functionArguments = function.arguments
    return if (functionName != null && functionArguments != null) {
      val result = assistant.getToolRegistered(functionName, functionArguments)
      val callId = toolCall.value.id
      if (callId != null) {
        callId to result
      } else null
    } else null
  }

  suspend fun getRun(runId: String): RunObject =
    api.getRun(threadId, runId, configure = ::defaultConfig)

  suspend fun createRun(assistant: Assistant): RunObject =
    createRun(CreateRunRequest(assistantId = assistant.assistantId))

  fun run(assistant: Assistant): Flow<RunDelta> =
    createRunStream(assistant, CreateRunRequest(assistantId = assistant.assistantId))

  suspend fun cancelRun(runId: String): RunObject =
    api.cancelRun(threadId, runId, configure = ::defaultConfig)

  suspend fun runSteps(runId: String): List<RunStepObject> =
    api.listRunSteps(threadId, runId, configure = ::defaultConfig).data

  private fun RunStepObjectStepDetails.toolCalls():
    List<RunStepDetailsToolCallsObjectToolCallsInner> =
    when (val step = this) {
      is RunStepObjectStepDetails.CaseRunStepDetailsMessageCreationObject -> emptyList()
      is RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject -> step.value.toolCalls
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
