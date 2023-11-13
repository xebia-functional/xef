package com.xebia.functional.xef.store

import com.xebia.functional.openai.models.ChatCompletionResponseMessage
import com.xebia.functional.openai.models.ChatCompletionRole
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage

sealed class MemorizedMessage {
  val role: ChatCompletionRole
    get() =
      when (this) {
        is Request -> ChatCompletionRole.valueOf(message.role().value)
        is Response -> ChatCompletionRole.valueOf(message.role.value)
      }

  fun asRequestMessage(): ChatCompletionRequestMessage =
    when (this) {
      is Request -> message
      is Response ->
        ChatCompletionRequestMessage.ChatCompletionRequestAssistantMessage(message.content)
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()

  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}
