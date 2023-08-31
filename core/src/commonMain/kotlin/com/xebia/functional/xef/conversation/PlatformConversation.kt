package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.Provider
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore

expect abstract class PlatformConversation(
  store: VectorStore,
  conversationId: ConversationId?,
  provider: Provider<*>,
) : Conversation {

  companion object {
    fun create(
      store: VectorStore,
      conversationId: ConversationId?,
      provider: Provider<*>,
    ): PlatformConversation
  }
}
