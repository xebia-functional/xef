package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.openai.models.FunctionObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantToolsFunction(val function: FunctionObject, val type: Type) : AssistantTools {
  constructor(function: FunctionObject) : this(function, Type.function)

  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "function") function("function")
  }
}
