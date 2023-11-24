package com.xebia.functional.openai.models.ext.assistant

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable(with = AssistantTools.MyTypeSerializer::class)
sealed interface AssistantTools {

  object MyTypeSerializer :
    JsonContentPolymorphicSerializer<AssistantTools>(AssistantTools::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AssistantTools> =
      when (element.jsonObject["type"]?.jsonPrimitive?.contentOrNull) {
        AssistantToolsCode.Type.code_interpreter.value -> AssistantToolsCode.serializer()
        AssistantToolsRetrieval.Type.retrieval.value -> AssistantToolsRetrieval.serializer()
        else -> AssistantToolsFunction.serializer()
      }
  }
}
