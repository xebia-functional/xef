package com.xebia.functional.xef.llm.models.chat

data class Message(val role: Role, val content: String, val name: String) {
  companion object {
    suspend fun systemMessage(message: suspend () -> String) =
      Message(role = Role.SYSTEM, content = message(), name = Role.SYSTEM.name)

    suspend fun userMessage(message: suspend () -> String) =
      Message(role = Role.USER, content = message(), name = Role.USER.name)

    suspend fun assistantMessage(message: suspend () -> String) =
      Message(role = Role.ASSISTANT, content = message(), name = Role.ASSISTANT.name)
  }
}
