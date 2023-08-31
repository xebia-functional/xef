package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.Provider
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore

class JSConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?,
  override val provider: Provider<*>,
) : PlatformConversation(store, conversationId, provider) {

  override val conversation: Conversation = this

  override fun close() {}

  override fun <A : AutoCloseable> autoClose(autoCloseable: A): A {
    return autoCloseable
  }
}
