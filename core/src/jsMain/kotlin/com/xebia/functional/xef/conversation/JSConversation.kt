package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher

class JSConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?,
  override val dispatcher: Dispatcher,
) : PlatformConversation(store, conversationId, dispatcher) {

  override val conversation: Conversation = this

  override fun close() {}

  override fun <A : AutoCloseable> autoClose(autoCloseable: A): A {
    return autoCloseable
  }
}
