package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestAssistantMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContentText

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
          listOf(
            ChatCompletionRequestUserMessageContentText(
              "Question $it${append?.let { ": $it" } ?: ""}"
            )
          )
        )
      val m2 = ChatCompletionRequestAssistantMessage("Response $it${append?.let { ": $it" } ?: ""}")
      listOf(
        Memory(conversationId, MemorizedMessage.Request(m1), atomicInt.addAndGet(1)),
        Memory(conversationId, MemorizedMessage.Request(m2), atomicInt.addAndGet(1)),
      )
    }
}
