package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.openai.models.RunStepDetailsMessageCreationObjectMessageCreation
import com.xebia.functional.openai.models.RunStepDetailsToolCallsObjectToolCallsInner
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable(with = RunStepObjectStepDetails.MyTypeSerializer::class)
sealed interface RunStepObjectStepDetails {

  object MyTypeSerializer :
    JsonContentPolymorphicSerializer<RunStepObjectStepDetails>(RunStepObjectStepDetails::class) {
    override fun selectDeserializer(
      element: JsonElement
    ): DeserializationStrategy<RunStepObjectStepDetails> =
      when (element.jsonObject["type"]?.jsonPrimitive?.contentOrNull) {
        RunStepDetailsMessageCreationObject.Type.message_creation.value ->
          RunStepDetailsMessageCreationObject.serializer()
        else -> RunStepDetailsToolCallsObject.serializer()
      }
  }
}

@Serializable
data class RunStepDetailsMessageCreationObject(
  @SerialName(value = "message_creation")
  @Required
  val messageCreation: RunStepDetailsMessageCreationObjectMessageCreation,
  /* Always `message_creation``. */
  @SerialName(value = "type") @Required val type: Type = Type.message_creation
) : RunStepObjectStepDetails {

  /**
   * Always `message_creation``.
   *
   * Values: message_creation
   */
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "message_creation") message_creation("message_creation")
  }
}

@Serializable
data class RunStepDetailsToolCallsObject(

  /* An array of tool calls the run step was involved in. These can be associated with one of three types of tools: `code_interpreter`, `retrieval`, or `function`.  */
  @SerialName(value = "tool_calls")
  @Required
  val toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>,

  /* Always `tool_calls`. */
  @SerialName(value = "type") @Required val type: Type = Type.tool_calls
) : RunStepObjectStepDetails {

  /**
   * Always `tool_calls`.
   *
   * Values: tool_calls
   */
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "tool_calls") tool_calls("tool_calls")
  }
}
