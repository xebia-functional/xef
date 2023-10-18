package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore

expect abstract class PlatformConversation(
  store: VectorStore,
  conversationId: ConversationId?,
) : Conversation {

  companion object {
    fun create(
      store: VectorStore,
      metric: Metric,
      conversationId: ConversationId?,
    ): PlatformConversation
  }
}
