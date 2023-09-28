package com.xebia.functional.xef.store

import com.xebia.functional.xef.llm.models.chat.Message
import kotlinx.serialization.Serializable

/**
 * Representation of the memory of a call in a conversation.
 *
 * @property conversationId uniquely identifies the conversation in which the message took place.
 * @property timestamp in milliseconds.
 * @property request list of messages sent by the user.
 * @property aiResponse list of messages sent by the assistant.
 * @property responseTimeInMillis time in milliseconds it took to generate the response.
 * @property tokens real tokens from the model. This include the tokens from the previous messages
 *   in the conversation
 */
data class Memory(
  val conversationId: ConversationId,
  val timestamp: Long,
  val request: List<MessageWithTokens>,
  val aiResponse: List<MessageWithTokens>,
  val responseTimeInMillis: Long,
  val tokens: Int
) {
  fun getSortedMessages(): List<Message> = (request + aiResponse).map { it.message }

  fun getApproxTokens(): Int = (request + aiResponse).map { it.approxTokens }.sum()
}

@Serializable data class MessageWithTokens(val message: Message, val approxTokens: Int)
