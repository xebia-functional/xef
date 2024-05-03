package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.ServerSentEvent
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.llm.assistants.RunDelta.Companion.RunDeltaEvent.*
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

  @JvmInline @Serializable value class Unknown(val event: ServerSentEvent) : RunDelta

  companion object {

    fun toServerSentEvent(runDelta: RunDelta): ServerSentEvent? {
      return when (runDelta) {
        is MessageCompleted -> runDelta.serverSentEventOf(ThreadMessageCompleted)
        is MessageCreated -> runDelta.serverSentEventOf(ThreadMessageCreated)
        is MessageDelta -> runDelta.serverSentEventOf(ThreadMessageDelta)
        is MessageInProgress -> runDelta.serverSentEventOf(ThreadMessageInProgress)
        is MessageIncomplete -> runDelta.serverSentEventOf(ThreadMessageIncomplete)
        is RunCancelled -> runDelta.serverSentEventOf(ThreadRunCancelled)
        is RunCancelling -> runDelta.serverSentEventOf(ThreadRunCancelling)
        is RunCompleted -> runDelta.serverSentEventOf(ThreadRunCompleted)
        is RunCreated -> runDelta.serverSentEventOf(ThreadRunCreated)
        is RunExpired -> runDelta.serverSentEventOf(ThreadRunExpired)
        is RunFailed -> runDelta.serverSentEventOf(ThreadRunFailed)
        is RunInProgress -> runDelta.serverSentEventOf(ThreadRunInProgress)
        is RunQueued -> runDelta.serverSentEventOf(ThreadRunQueued)
        is RunRequiresAction -> runDelta.serverSentEventOf(ThreadRunRequiresAction)
        is RunStepCancelled -> runDelta.serverSentEventOf(ThreadRunStepCancelled)
        is RunStepCompleted -> runDelta.serverSentEventOf(ThreadRunStepCompleted)
        is RunStepCreated -> runDelta.serverSentEventOf(ThreadRunStepCreated)
        is RunStepDelta -> runDelta.serverSentEventOf(ThreadRunStepDelta)
        is RunStepExpired -> runDelta.serverSentEventOf(ThreadRunStepExpired)
        is RunStepFailed -> runDelta.serverSentEventOf(ThreadRunStepFailed)
        is RunStepInProgress -> runDelta.serverSentEventOf(ThreadRunStepInProgress)
        is RunSubmitToolOutputs -> null
        is ThreadCreated -> runDelta.serverSentEventOf(RunDeltaEvent.ThreadCreated)
        is Unknown -> runDelta.event
      }
    }

    private inline fun <reified Delta : RunDelta> Delta.serverSentEventOf(
      event: RunDeltaEvent
    ): ServerSentEvent =
      ServerSentEvent(
        event = event.value,
        data = Config.DEFAULT.json.encodeToJsonElement(serializer(), this)
      )

    fun fromServerSentEvent(serverEvent: ServerSentEvent): RunDelta {
      val data = serverEvent.data ?: error("Expected data in ServerSentEvent for RunDelta")
      val type = serverEvent.event ?: error("Expected event in ServerSentEvent for RunDelta")
      val event =
        RunDeltaEvent.values().find {
          type.replace(".", "").replace("_", "").equals(it.name, ignoreCase = true)
        }
      val json = Config.DEFAULT.json
      return when (event) {
        RunDeltaEvent.ThreadCreated ->
          ThreadCreated(json.decodeFromJsonElement(ThreadObject.serializer(), data))
        ThreadRunCreated -> RunCreated(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunQueued -> RunQueued(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunInProgress ->
          RunInProgress(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunRequiresAction ->
          RunRequiresAction(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunCompleted -> RunCompleted(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunFailed -> RunFailed(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunCancelling ->
          RunCancelling(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunCancelled -> RunCancelled(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunExpired -> RunExpired(json.decodeFromJsonElement(RunObject.serializer(), data))
        ThreadRunStepCreated ->
          RunStepCreated(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        ThreadRunStepInProgress ->
          RunStepInProgress(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        ThreadRunStepDelta ->
          RunStepDelta(json.decodeFromJsonElement(RunStepDeltaObject.serializer(), data))
        ThreadRunStepCompleted ->
          RunStepCompleted(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        ThreadRunStepFailed ->
          RunStepFailed(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        ThreadRunStepCancelled ->
          RunStepCancelled(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        ThreadRunStepExpired ->
          RunStepExpired(json.decodeFromJsonElement(RunStepObject.serializer(), data))
        ThreadMessageCreated ->
          MessageCreated(json.decodeFromJsonElement(MessageObject.serializer(), data))
        ThreadMessageInProgress ->
          MessageInProgress(json.decodeFromJsonElement(MessageObject.serializer(), data))
        ThreadMessageDelta ->
          MessageDelta(json.decodeFromJsonElement(MessageDeltaObject.serializer(), data))
        ThreadMessageCompleted ->
          MessageCompleted(json.decodeFromJsonElement(MessageObject.serializer(), data))
        ThreadMessageIncomplete ->
          MessageIncomplete(json.decodeFromJsonElement(MessageObject.serializer(), data))
        RunDeltaEvent.Error -> Unknown(serverEvent)
        null -> Unknown(serverEvent)
      }
    }

    enum class RunDeltaEvent(val value: String) {
      ThreadCreated("thread.created"),
      ThreadRunCreated("thread.run.created"),
      ThreadRunQueued("thread.run.queued"),
      ThreadRunInProgress("thread.run.in_progress"),
      ThreadRunRequiresAction("thread.run.requires_action"),
      ThreadRunCompleted("thread.run.completed"),
      ThreadRunFailed("thread.run.failed"),
      ThreadRunCancelling("thread.run.cancelling"),
      ThreadRunCancelled("thread.run.cancelled"),
      ThreadRunExpired("thread.run.expired"),
      ThreadRunStepCreated("thread.run.step.created"),
      ThreadRunStepInProgress("thread.run.step.in_progress"),
      ThreadRunStepDelta("thread.run.step.delta"),
      ThreadRunStepCompleted("thread.run.step.completed"),
      ThreadRunStepFailed("thread.run.step.failed"),
      ThreadRunStepCancelled("thread.run.step.cancelled"),
      ThreadRunStepExpired("thread.run.step.expired"),
      ThreadMessageCreated("thread.message.created"),
      ThreadMessageInProgress("thread.message.in_progress"),
      ThreadMessageDelta("thread.message.delta"),
      ThreadMessageCompleted("thread.message.completed"),
      ThreadMessageIncomplete("thread.message.incomplete"),
      Error("error")
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
                is RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsRetrievalObject -> {
                  val retrieval = it.value.retrieval
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
