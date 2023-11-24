package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.openai.models.FunctionObject
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

@Serializable
data class AssistantToolsCode(val type: Type = Type.code_interpreter) : AssistantTools {
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "code_interpreter") code_interpreter("code_interpreter")
  }
}

@Serializable
data class AssistantToolsRetrieval(val type: Type = Type.retrieval) : AssistantTools {
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "retrieval") retrieval("retrieval")
  }
}

@Serializable
data class AssistantToolsFunction(val type: Type = Type.function, val function: FunctionObject) :
  AssistantTools {
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "function") function("function")
  }
}
