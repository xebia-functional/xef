package com.xebia.functional.xef.auto

import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore

expect abstract class PlatformConversation(
  store: VectorStore,
  conversationId: ConversationId?,
) : Conversation {

  companion object {
    fun create(
      store: VectorStore,
      conversationId: ConversationId?,
    ): PlatformConversation
  }
}
