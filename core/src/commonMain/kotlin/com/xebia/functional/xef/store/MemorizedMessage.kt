package com.xebia.functional.xef.store

import com.xebia.functional.xef.prompt.completionRole
import io.github.nomisrev.openapi.*

sealed class MemorizedMessage {
  val role: ChatCompletionRole
    get() =
      when (this) {
        is Request -> ChatCompletionRole.valueOf(message.completionRole().value)
        is Response -> ChatCompletionRole.valueOf(message.role.name)
      }

  fun asRequestMessage(): ChatCompletionRequestMessage =
    when (this) {
      is Request -> message
      is Response ->
        ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
          ChatCompletionRequestAssistantMessage(
            role = ChatCompletionRequestAssistantMessage.Role.Assistant,
            // TODO: Find a new strategy to save the tool calls as content
            content = message.content ?: message.toolCalls?.firstOrNull()?.toString()
          )
        )
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()

  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}

fun memorizedMessage(role: ChatCompletionRole, content: String): MemorizedMessage =
  when (role) {
    ChatCompletionRole.System ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage(
          ChatCompletionRequestSystemMessage(
            content = content,
            role = ChatCompletionRequestSystemMessage.Role.System
          )
        )
      )
    ChatCompletionRole.User ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
          ChatCompletionRequestUserMessage(
            content = ChatCompletionRequestUserMessage.Content.CaseString(content),
            role = ChatCompletionRequestUserMessage.Role.User
          )
        )
      )
    ChatCompletionRole.Assistant ->
      MemorizedMessage.Response(
        ChatCompletionResponseMessage(
          content = content,
          role = ChatCompletionResponseMessage.Role.Assistant
        )
      )
    ChatCompletionRole.Tool -> error("Tool messages are not supported")
    ChatCompletionRole.Function -> error("Function messages are not supported")
  }
