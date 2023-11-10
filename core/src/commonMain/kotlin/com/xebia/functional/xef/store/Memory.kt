package com.xebia.functional.xef.store

/**
 * Representation of the memory of a message in a conversation.
 *
 * @property content message sent.
 * @property conversationId uniquely identifies the conversation in which the message took place.
 * @property index autoincrement index.
 */
data class Memory(val conversationId: ConversationId, val content: MemorizedMessage, val index: Int)

