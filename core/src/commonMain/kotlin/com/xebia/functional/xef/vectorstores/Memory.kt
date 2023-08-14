package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.llm.models.chat.Message

/**
 * Representation of the memory of a message in a conversation.
 *
 * @property content message sent.
 * @property conversationId uniquely identifies the conversation in which the message took place.
 * @property timestamp in milliseconds.
 */
data class Memory(
  val conversationId: ConversationId,
  val content: Message,
  val timestamp: Long,
  val approxTokens: Int
)
