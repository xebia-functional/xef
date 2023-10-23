package com.xebia.functional.xef.conversation

import kotlinx.serialization.Serializable

/**
 * The [MessagePolicy] encapsulates the message selection policy for sending to the server. Allows
 * defining the percentages of historical and contextual messages to include in the final list.
 *
 * @property historyPercent Percentage of historical messages
 * @property historyPaddingTokens Number of tokens added when getting the limit of tokens from the
 *   VectorStore
 * @property contextPercent Percentage of context messages
 */
@Serializable
data class MessagePolicy(
  var historyPercent: Int = 50,
  var historyPaddingTokens: Int = 100,
  var contextPercent: Int = 50,
  var addMessagesFromConversation: MessagesFromHistory = MessagesFromHistory.ALL,
  var addMessagesToConversation: MessagesToHistory = MessagesToHistory.ALL,
)

enum class MessagesFromHistory {
  ALL,
  NONE,
}

enum class MessagesToHistory {
  ALL,
  ONLY_SYSTEM_MESSAGES,
  NOT_SYSTEM_MESSAGES,
  NONE,
}
