package com.xebia.functional.xef.prompt

import arrow.core.raise.Raise
import arrow.core.raise.either
import kotlinx.serialization.Serializable

enum class Type {
  human,
  ai,
  system,
  chat
}

@Serializable
sealed class Message {
  abstract val content: String

  abstract fun format(): String

  fun type(): Type =
    when (this) {
      is HumanMessage -> Type.human
      is AIMessage -> Type.ai
      is SystemMessage -> Type.system
      is ChatMessage -> Type.chat
    }
}

data class HumanMessage(override val content: String) : Message() {
  override fun format(): String = "${type().name.capitalized()}: $content"
}

data class AIMessage(override val content: String) : Message() {
  override fun format(): String = "${type().name.uppercase()}: $content"
}

data class SystemMessage(override val content: String) : Message() {
  override fun format(): String = "${type().name.capitalized()}: $content"
}

data class ChatMessage(override val content: String, val role: String) : Message() {
  override fun format(): String = "$role: $content"
}

enum class TemplateFormat {
  FString
}



