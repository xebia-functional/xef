package com.xebia.functional.xef.auto

import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore
import java.io.Closeable

open class JVMConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?,
) :
  PlatformConversation(store, conversationId), AutoClose by autoClose(), AutoCloseable, Closeable {

  override val conversation: Conversation = this
}
