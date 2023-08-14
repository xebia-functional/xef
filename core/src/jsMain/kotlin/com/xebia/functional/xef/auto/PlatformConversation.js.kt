package com.xebia.functional.xef.auto

import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore

actual abstract class PlatformConversation
actual constructor(store: VectorStore, conversationId: ConversationId?) : Conversation, AutoClose {
  actual companion object {
    actual fun create(store: VectorStore, conversationId: ConversationId?): PlatformConversation =
      JSConversation(store, conversationId)
  }
}
