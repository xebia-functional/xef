package com.xebia.functional.xef.store

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
    val m1 = Message(Role.USER, "Question $it${append?.let { ": $it" } ?: ""}", "USER")
    val m2 = Message(Role.ASSISTANT, "Response $it${append?.let { ": $it" } ?: ""}", "ASSISTANT")
    listOf(
      Memory(conversationId, m1, startTimestamp + (it * 10), calculateTokens(m1)),
      Memory(conversationId, m2, startTimestamp + (it * 10) + 1, calculateTokens(m2)),
    )
  }

fun calculateTokens(message: Message): Int =
  message.content.split(" ").size + 2 // 2 is the role and name
