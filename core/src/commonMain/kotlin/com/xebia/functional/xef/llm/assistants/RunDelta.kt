package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.ServerSentEvent
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.Config
import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
sealed interface RunDelta {

  /** [RunDeltaEvent.thread_created] */
  @JvmInline @Serializable value class ThreadCreated(val thread: ThreadObject) : RunDelta

  /** [RunDeltaEvent.thread_run_created] */
  @JvmInline @Serializable value class RunCreated(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_queued] */
  @JvmInline @Serializable value class RunQueued(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_in_progress] */
  @JvmInline @Serializable value class RunInProgress(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_requires_action] */
  @JvmInline @Serializable value class RunRequiresAction(val run: RunObject) : RunDelta

  @JvmInline
  @Serializable
  value class RunSubmitToolOutputs(val run: SubmitToolOutputsRunRequest) : RunDelta

  /** [RunDeltaEvent.thread_run_completed] */
  @JvmInline @Serializable value class RunCompleted(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_failed] */
  @JvmInline @Serializable value class RunFailed(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_cancelling] */
  @JvmInline @Serializable value class RunCancelling(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_cancelled] */
  @JvmInline @Serializable value class RunCancelled(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_expired] */
  @JvmInline @Serializable value class RunExpired(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_incomplete] */
  @JvmInline @Serializable value class RunIncomplete(val run: RunObject) : RunDelta

  /** [RunDeltaEvent.thread_run_step_created] */
  @JvmInline @Serializable value class RunStepCreated(val runStep: RunStepObject) : RunDelta

  /** [RunDeltaEvent.thread_run_step_in_progress] */
  @JvmInline @Serializable value class RunStepInProgress(val runStep: RunStepObject) : RunDelta

  /** [RunDeltaEvent.thread_run_step_delta] */
  @JvmInline
  @Serializable
  value class RunStepDelta(val runStepDelta: RunStepDeltaObject) : RunDelta

  /**
   * { "id": "step_123", "object": "thread.run.step.delta", "delta": { "step_details": { "type":
   * "tool_calls", "tool_calls":
   * [ { "index": 0, "id": "call_123", "type": "code_interpreter", "code_interpreter": { "input": "", "outputs": []
   * } } ] } } }
   */
  @Serializable
  data class RunStepDeltaObject(
    val id: String,
    val `object`: String,
    val delta: RunStepDeltaObjectInner
  )

  @Serializable
  data class RunStepDeltaObjectInner(
    @SerialName("step_details") val stepDetails: RunStepObjectStepDetails
  )

  /** [RunDeltaEvent.thread_run_step_completed] */
  @JvmInline @Serializable value class RunStepCompleted(val runStep: RunStepObject) : RunDelta

  /** [RunDeltaEvent.thread_run_step_failed] */
  @JvmInline @Serializable value class RunStepFailed(val runStep: RunStepObject) : RunDelta

  /** [RunDeltaEvent.thread_run_step_cancelled] */
  @JvmInline @Serializable value class RunStepCancelled(val runStep: RunStepObject) : RunDelta

  /** [RunDeltaEvent.thread_run_step_expired] */
  @JvmInline @Serializable value class RunStepExpired(val runStep: RunStepObject) : RunDelta

  /** [RunDeltaEvent.thread_message_created] */
  @JvmInline @Serializable value class MessageCreated(val message: MessageObject) : RunDelta

  /** [RunDeltaEvent.thread_message_in_progress] */
  @JvmInline @Serializable value class MessageInProgress(val message: MessageObject) : RunDelta

  /** [RunDeltaEvent.thread_message_delta] */
  @JvmInline
  @Serializable
  value class MessageDelta(val messageDelta: MessageDeltaObject) : RunDelta

  /**
   * { "id": "msg_123", "object": "thread.message.delta", "delta": { "content":
   * [ { "index": 0, "type": "text", "text": { "value": "Hello", "annotations": [] } } ] } }
   */
  @Serializable
  data class MessageDeltaObject(
    val id: String,
    val `object`: String,
    val delta: MessageDeltaObjectInner
  )

  @Serializable
  data class MessageDeltaObjectInner(val content: List<MessageDeltaObjectInnerContent>)

  @Serializable
  data class MessageDeltaObjectInnerContent(
    val index: Int,
    val type: String,
    val text: MessageDeltaObjectInnerContentText
  )

  @Serializable
  data class MessageDeltaObjectInnerContentText(
    val value: String,
    val annotations: List<JsonObject>? = null
  )

  /** [RunDeltaEvent.thread_message_completed] */
  @JvmInline @Serializable value class MessageCompleted(val message: MessageObject) : RunDelta

  /** [RunDeltaEvent.thread_message_incomplete] */
  @JvmInline @Serializable value class MessageIncomplete(val message: MessageObject) : RunDelta

  /** [RunDeltaEvent.error] */
  @JvmInline
  @Serializable
  value class Error(val error: com.xebia.functional.openai.generated.model.Error) : RunDelta

  @JvmInline @Serializable value class Unknown(val event: ServerSentEvent) : RunDelta

  companion object {
    private val typeExcludedCharactersRegex = "[._]".toRegex()

    fun fromServerSentEvent(serverEvent: ServerSentEvent): RunDelta {
      val data = serverEvent.data ?: error("Expected data in ServerSentEvent for RunDelta")
      val type = serverEvent.event ?: error("Expected event in ServerSentEvent for RunDelta")
      val event =
        RunDeltaEvent.entries.find {
          type.replace(typeExcludedCharactersRegex, "").equals(it.name, ignoreCase = true)
        }
      val json = Config.DEFAULT.json
      return when (event) {
        RunDeltaEvent.ThreadCreated ->
          ThreadCreated(json.decodeFromJsonElement(ThreadObject.serializer(), data))
        RunDeltaEvent.ThreadRunCreated ->
          RunCreated(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunQueued ->
          RunQueued(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunInProgress ->
          RunInProgress(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunRequiresAction ->
          RunRequiresAction(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunCompleted ->
          RunCompleted(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunFailed ->
          RunFailed(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunCancelling ->
          RunCancelling(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunCancelled ->
          RunCancelled(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunExpired ->
          RunExpired(json.decodeFromJsonElement(RunObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepCreated ->
          RunStepCreated(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepInProgress ->
          RunStepInProgress(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepDelta ->
          RunStepDelta(json.decodeFromJsonElement(RunStepDeltaObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepCompleted ->
          RunStepCompleted(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepFailed ->
          RunStepFailed(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepCancelled ->
          RunStepCancelled(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        RunDeltaEvent.ThreadRunStepExpired ->
          RunStepExpired(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        RunDeltaEvent.ThreadMessageCreated ->
          MessageCreated(json.decodeFromJsonElement(MessageObject.serializer(), data))
        RunDeltaEvent.ThreadMessageInProgress ->
          MessageInProgress(json.decodeFromJsonElement(MessageObject.serializer(), data))
        RunDeltaEvent.ThreadMessageDelta ->
          MessageDelta(json.decodeFromJsonElement(MessageDeltaObject.serializer(), data))
        RunDeltaEvent.ThreadMessageCompleted ->
          MessageCompleted(json.decodeFromJsonElement(MessageObject.serializer(), data))
        RunDeltaEvent.ThreadMessageIncomplete ->
          MessageIncomplete(json.decodeFromJsonElement(MessageObject.serializer(), data))
        RunDeltaEvent.Error ->
          Error(
            json.decodeFromJsonElement(
              com.xebia.functional.openai.generated.model.Error.serializer(),
              data
            )
          )
        null -> Unknown(serverEvent)
      }
    }

    enum class RunDeltaEvent {
      ThreadCreated,
      ThreadRunCreated,
      ThreadRunQueued,
      ThreadRunInProgress,
      ThreadRunRequiresAction,
      ThreadRunCompleted,
      ThreadRunFailed,
      ThreadRunCancelling,
      ThreadRunCancelled,
      ThreadRunExpired,
      ThreadRunStepCreated,
      ThreadRunStepInProgress,
      ThreadRunStepDelta,
      ThreadRunStepCompleted,
      ThreadRunStepFailed,
      ThreadRunStepCancelled,
      ThreadRunStepExpired,
      ThreadMessageCreated,
      ThreadMessageInProgress,
      ThreadMessageDelta,
      ThreadMessageCompleted,
      ThreadMessageIncomplete,
      Error
    }
  }

  fun printEvent(showData: Boolean = false) {
    val event = this::class.simpleName
    when (this) {
      is MessageInProgress -> {
        println("Event: $event")
        println("****")
      }
      is MessageDelta -> messageDelta.delta.content.forEach { content -> print(content.text.value) }
      is MessageCompleted -> {
        println()
        println("****")
        println("Event: $event")
      }
      is RunStepCompleted -> {
        when (val details = runStep.stepDetails) {
          is RunStepObjectStepDetails.CaseRunStepDetailsMessageCreationObject ->
            println("Creating msg: ${details.value.messageCreation.messageId}")
          is RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject ->
            println(
              "Tool calls: ${details.value.toolCalls.map { 
              when (it) {
                is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsCodeObject -> 
                  "Code: ${it.value.codeInterpreter.input}"
                is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject -> {
                  val function = it.value.function
                  "Function: ${function.name}(${function.arguments})"
                }
                is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFileSearchObject -> {
                  val retrieval = it.value.fileSearch
                  "Retrieval: $retrieval"
                }
              }
            }}"
            )
        }
      }
      is RunSubmitToolOutputs -> {
        run.toolOutputs.forEach { println("Tool output: ${it.toolCallId} - ${it.output}") }
      }
      else -> {
        val data = Json.encodeToString(serializer(), this)
        if (showData) println("Event: $event - Data: $data") else println("Event: $event")
      }
    }
  }
}
