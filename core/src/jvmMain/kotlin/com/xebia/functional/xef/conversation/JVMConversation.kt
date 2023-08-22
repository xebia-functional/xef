package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher
import java.io.Closeable

open class JVMConversation(
  override val store: VectorStore,
  override val conversationId: ConversationId?,
  override val dispatcher: Dispatcher
) :
  PlatformConversation(store, conversationId, dispatcher), AutoClose by autoClose(), AutoCloseable, Closeable {

  override val conversation: Conversation = this
}
