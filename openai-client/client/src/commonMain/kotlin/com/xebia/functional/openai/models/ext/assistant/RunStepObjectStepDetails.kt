package com.xebia.functional.openai.models.ext.assistant

import kotlinx.serialization.DeserializationStrategy
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
