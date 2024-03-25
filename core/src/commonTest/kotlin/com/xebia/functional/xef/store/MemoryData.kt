package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.openai.generated.model.ChatCompletionRequestAssistantMessage
import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessage
import com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessageContent

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
            ChatCompletionRequestUserMessageContent.First(
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
          MemorizedMessage.Request(ChatCompletionRequestMessage.Fifth(m1)),
          atomicInt.addAndGet(1)
        ),
        Memory(
          conversationId,
          MemorizedMessage.Request(ChatCompletionRequestMessage.First(m2)),
          atomicInt.addAndGet(1)
        ),
      )
    }
}
