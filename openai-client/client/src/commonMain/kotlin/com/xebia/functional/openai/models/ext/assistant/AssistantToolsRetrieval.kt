package com.xebia.functional.openai.models.ext.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantToolsRetrieval(val type: Type) : AssistantTools {
  constructor() : this(Type.retrieval)

  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "retrieval") retrieval("retrieval")
  }
}
