package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.Provider
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import java.io.Closeable

open class JVMConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?,
  override val provider: Provider<*>,
) :
  PlatformConversation(store, conversationId, provider), AutoClose by autoClose(), AutoCloseable, Closeable {

  override val conversation: Conversation = this
}
