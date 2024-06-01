package com.xebia.functional.xef.store

import com.xebia.functional.xef.llm.ChatCompletionRequestMessage
import com.xebia.functional.xef.llm.ChatCompletionResponseMessage
import com.xebia.functional.xef.llm.Role
import com.xebia.functional.xef.llm.ToolCallResults

sealed class MemorizedMessage {
  val role: Role
    get() =
      when (this) {
        is Request -> message.role
        is Response -> message.role
      }

  fun asRequestMessage(): ChatCompletionRequestMessage =
    when (this) {
      is Request -> message
      is Response ->
        ChatCompletionRequestMessage(
          role = role,
          content = message.content ?: "",
          toolCallResults = message.toolCalls?.map {
            ToolCallResults(
              toolCallId = it.id,
              toolCallName = it.function.functionName,
              result = it.function.arguments
            )
          }?.firstOrNull()
        )
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()

  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}

fun memorizedMessage(role: Role, content: String): MemorizedMessage =
  when (role) {
    Role.system ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage(
          content = content,
          role = Role.system,
          toolCallResults = null
        )
      )
    Role.user ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage(
          content = content,
          role = Role.user,
          toolCallResults = null
        )
      )
    Role.assistant ->
      MemorizedMessage.Response(
        ChatCompletionResponseMessage(
          content = content,
          role = Role.assistant
        )
      )
    Role.tool -> error("Tool messages are not supported")
  }
