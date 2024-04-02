package com.xebia.functional.xef.store

import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.prompt.completionRole

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
            role = ChatCompletionRequestAssistantMessage.Role.assistant,
            content = message.content ?: message.toolCalls?.firstOrNull()?.toString()
          )
        )
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()

  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}

fun memorizedMessage(role: ChatCompletionRole, content: String): MemorizedMessage =
  when (role) {
    ChatCompletionRole.Supported.system ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage(
          ChatCompletionRequestSystemMessage(
            content = content,
            role = ChatCompletionRequestSystemMessage.Role.system
          )
        )
      )
    ChatCompletionRole.Supported.user ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
          ChatCompletionRequestUserMessage(
            content = ChatCompletionRequestUserMessageContent.CaseString(content),
            role = ChatCompletionRequestUserMessage.Role.user
          )
        )
      )
    ChatCompletionRole.Supported.assistant ->
      MemorizedMessage.Response(
        ChatCompletionResponseMessage(
          content = content,
          role = ChatCompletionResponseMessage.Role.assistant
        )
      )
    ChatCompletionRole.Supported.tool -> error("Tool messages are not supported")
    ChatCompletionRole.Supported.function -> error("Function messages are not supported")
    is ChatCompletionRole.Custom -> error("Custom messages are not supported")
  }
