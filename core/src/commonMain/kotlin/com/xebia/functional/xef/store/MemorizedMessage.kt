package com.xebia.functional.xef.store

import com.xebia.functional.openai.models.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ChatCompletionResponseMessage
import com.xebia.functional.openai.models.ChatCompletionRole

sealed class MemorizedMessage {
  val role: ChatCompletionRole
    get() = when (this) {
      is Request -> ChatCompletionRole.valueOf(message.role.value)
      is Response ->  ChatCompletionRole.valueOf(message.role.value)
    }

  fun asRequestMessage(): ChatCompletionRequestMessage =
    when (this) {
      is Request -> message
      is Response -> ChatCompletionRequestMessage(
        content = message.content,
        role = ChatCompletionRequestMessage.Role.function,

      )
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()
  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}
