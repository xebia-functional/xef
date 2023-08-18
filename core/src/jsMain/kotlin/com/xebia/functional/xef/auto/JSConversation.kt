package com.xebia.functional.xef.auto

import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore

class JSConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?
) : PlatformConversation(store, conversationId) {

  override val conversation: Conversation = this

  override fun close() {}

  override fun <A : AutoCloseable> autoClose(autoCloseable: A): A {
    return autoCloseable
  }
}
