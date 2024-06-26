package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.xef.openapi.ChatCompletionRequestAssistantMessage
import com.xebia.functional.xef.openapi.ChatCompletionRequestMessage
import com.xebia.functional.xef.openapi.ChatCompletionRequestUserMessage

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
          role = ChatCompletionRequestUserMessage.Role.User,
          content =
            ChatCompletionRequestUserMessage.Content.CaseString(
              "Question $it${append?.let { ": $it" } ?: ""}"
            )
        )
      val m2 =
        ChatCompletionRequestAssistantMessage(
          role = ChatCompletionRequestAssistantMessage.Role.Assistant,
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
