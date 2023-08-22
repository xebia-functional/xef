package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher

actual abstract class PlatformConversation
actual constructor(store: VectorStore, conversationId: ConversationId?, dispatcher: Dispatcher) : Conversation, AutoClose {
  actual companion object {
    actual fun create(store: VectorStore, conversationId: ConversationId?, dispatcher: Dispatcher): PlatformConversation =
      JSConversation(store, conversationId, dispatcher)
  }
}
