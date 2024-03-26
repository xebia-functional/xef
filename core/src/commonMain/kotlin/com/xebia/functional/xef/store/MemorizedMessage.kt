package com.xebia.functional.xef.store

import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.prompt.completionRole

sealed class MemorizedMessage {
  val role: ChatCompletionRole
    get() =
      when (this) {
        is Request -> ChatCompletionRole.valueOf(message.completionRole().value)
        is Response -> ChatCompletionRole.valueOf(message.role.value)
      }

  fun asRequestMessage(): ChatCompletionRequestMessage =
    when (this) {
      is Request -> message
      is Response ->
        ChatCompletionRequestMessage.First(
          ChatCompletionRequestAssistantMessage(
            role = ChatCompletionRequestAssistantMessage.Role.assistant,
            content = message.content
          )
        )
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()

  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}

fun memorizedMessage(role: ChatCompletionRole, content: String): MemorizedMessage =
  when (role) {
    ChatCompletionRole.system ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.Third(
          ChatCompletionRequestSystemMessage(
            content = content,
            role = ChatCompletionRequestSystemMessage.Role.system
          )
        )
      )
    ChatCompletionRole.user ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.Fifth(
          ChatCompletionRequestUserMessage(
            content = ChatCompletionRequestUserMessageContent.First(content),
            role = ChatCompletionRequestUserMessage.Role.user
          )
        )
      )
    ChatCompletionRole.assistant ->
      MemorizedMessage.Response(
        ChatCompletionResponseMessage(
          content = content,
          role = ChatCompletionResponseMessage.Role.assistant
        )
      )
    ChatCompletionRole.tool -> error("Tool messages are not supported")
    ChatCompletionRole.function -> error("Function messages are not supported")
  }
