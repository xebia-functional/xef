package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

class MemoryData {
  val defaultConversationId = ConversationId("default-id")

  val atomicInt = AtomicInt(0)

  fun generateRandomMessages(
    n: Int,
    append: String? = null,
    conversationId: ConversationId = defaultConversationId
  ): List<Memory> =
    (0 until n).flatMap {
      val m1 = Message(Role.USER, "Question $it${append?.let { ": $it" } ?: ""}", "USER")
      val m2 = Message(Role.ASSISTANT, "Response $it${append?.let { ": $it" } ?: ""}", "ASSISTANT")
      listOf(
        Memory(conversationId, m1, atomicInt.addAndGet(1)),
        Memory(conversationId, m2, atomicInt.addAndGet(1)),
      )
    }
}
