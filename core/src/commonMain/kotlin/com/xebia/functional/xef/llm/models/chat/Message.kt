package com.xebia.functional.xef.llm.models.chat

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable

@Serializable
data class Message(val role: Role, val content: String, val name: String) {
  companion object {
    @JvmStatic
    fun apply(role: Role, content: String, name: String): Message = Message(role, content, name)
  }
}
