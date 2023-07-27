package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

val defaultConversationId = ConversationId("default-id")

fun generateRandomMessages(
  n: Int,
  append: String? = null,
  conversationId: ConversationId = defaultConversationId,
  startTimestamp: Long = 0
): List<Memory> =
  (0 until n).flatMap {
    listOf(
      Memory(
        conversationId,
        Message(Role.USER, "Question $it${append?.let { ": $it" } ?: ""}", "USER"),
        startTimestamp + (it * 10)
      ),
      Memory(
        conversationId,
        Message(Role.ASSISTANT, "Response $it${append?.let { ": $it" } ?: ""}", "ASSISTANT"),
        startTimestamp + (it * 10) + 1
      ),
    )
  }
