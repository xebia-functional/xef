package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import io.github.nomisrev.openapi.ChatCompletionRequestAssistantMessage
import io.github.nomisrev.openapi.ChatCompletionRequestMessage
import io.github.nomisrev.openapi.ChatCompletionRequestUserMessage
import io.github.nomisrev.openapi.ChatCompletionRequestUserMessageContent

class MemoryData {
  val defaultConversationId = ConversationId("default-id")

  val atomicInt = AtomicInt(0)

  fun generateRandomMessages(
    n: Int,
    append: String? = null,
    conversationId: ConversationId = defaultConversationId
  ): List<Memory> =
    (0 until n).flatMap {
      val m1 =
        ChatCompletionRequestUserMessage(
          role = ChatCompletionRequestUserMessage.Role.user,
          content =
            ChatCompletionRequestUserMessageContent.CaseString(
              "Question $it${append?.let { ": $it" } ?: ""}"
            )
        )
      val m2 =
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.assistant,
          content = "Response $it${append?.let { ": $it" } ?: ""}"
        )
      listOf(
        Memory(
          conversationId,
          MemorizedMessage.Request(
            ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(m1)
          ),
          atomicInt.addAndGet(1)
        ),
        Memory(
          conversationId,
          MemorizedMessage.Request(
            ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(m2)
          ),
          atomicInt.addAndGet(1)
        ),
      )
    }
}
