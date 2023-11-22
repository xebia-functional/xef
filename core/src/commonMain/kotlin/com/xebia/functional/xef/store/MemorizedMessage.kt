package com.xebia.functional.xef.store

import com.xebia.functional.openai.models.ChatCompletionResponseMessage
import com.xebia.functional.openai.models.ChatCompletionRole
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContent
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContentText

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
        ChatCompletionRequestMessage.ChatCompletionRequestAssistantMessage(message.content)
    }

  data class Request(val message: ChatCompletionRequestMessage) : MemorizedMessage()

  data class Response(val message: ChatCompletionResponseMessage) : MemorizedMessage()
}

fun memorizedMessage(role: ChatCompletionRole, content: String): MemorizedMessage =
  when (role) {
    ChatCompletionRole.system ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.ChatCompletionRequestSystemMessage(content)
      )

    ChatCompletionRole.user ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.ChatCompletionRequestUserMessage(
          listOf(
            ChatCompletionRequestUserMessageContentText(
              ChatCompletionRequestUserMessageContentText.Type.text,
              content
            )
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

    ChatCompletionRole.tool ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.ChatCompletionRequestToolMessage(
          content = content,
          toolCallId = "fake-tool-call-id" // TODO we are not storing the tool id with the content
        )
      )

    ChatCompletionRole.function ->
      MemorizedMessage.Request(
        ChatCompletionRequestMessage.ChatCompletionRequestToolMessage(
          content = content,
          toolCallId = "fake-tool-call-id" // TODO we are not storing the tool id with the content
        )
      )
  }
