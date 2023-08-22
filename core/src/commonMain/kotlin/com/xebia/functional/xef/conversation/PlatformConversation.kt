package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher

expect abstract class PlatformConversation(
  store: VectorStore,
  conversationId: ConversationId?,
  dispatcher: Dispatcher,
) : Conversation {

  companion object {
    fun create(
      store: VectorStore,
      conversationId: ConversationId?,
      dispatcher: Dispatcher,
    ): PlatformConversation
  }
}
