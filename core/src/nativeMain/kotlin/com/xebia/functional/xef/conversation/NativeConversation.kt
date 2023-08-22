package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore

class NativeConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?,
) : PlatformConversation(store, conversationId) {

  override val conversation: Conversation = this

  override fun close() {}

  override fun <A : AutoCloseable> autoClose(autoCloseable: A): A {
    return autoCloseable
  }
}
