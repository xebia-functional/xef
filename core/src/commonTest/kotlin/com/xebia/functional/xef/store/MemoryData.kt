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
  (0 until n).map {
    val m1 = Message(Role.USER, "Question $it${append?.let { ": $it" } ?: ""}", "USER")
    val m2 = Message(Role.ASSISTANT, "Response $it${append?.let { ": $it" } ?: ""}", "ASSISTANT")
    val tokensM1 = calculateTokens(m1)
    val tokensM2 = calculateTokens(m2)

    Memory(
      conversationId,
      startTimestamp + (it * 10),
      listOf(MessageWithTokens(m1, tokensM1)),
      listOf(MessageWithTokens(m2, tokensM2)),
      100,
      tokensM1 + tokensM2
    )
  }

fun calculateTokens(message: Message): Int =
  message.content.split(" ").size + 2 // 2 is the role and name
