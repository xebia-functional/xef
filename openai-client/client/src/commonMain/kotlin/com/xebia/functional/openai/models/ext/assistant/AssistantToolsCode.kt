package com.xebia.functional.openai.models.ext.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantToolsCode(val type: Type) : AssistantTools {
  constructor() : this(Type.code_interpreter)

  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "code_interpreter") code_interpreter("code_interpreter");

    override fun toString(): String {
      return value
    }
  }
}
