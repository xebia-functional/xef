package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore

actual abstract class PlatformConversation
actual constructor(store: VectorStore, conversationId: ConversationId?) :
  Conversation, AutoClose, AutoCloseable {
  actual companion object {
    actual suspend fun create(
      store: VectorStore,
      metric: Metric,
      conversationId: ConversationId?
    ): PlatformConversation {
      conversationId?.let { cid -> store.updateIndexByConversationId(cid) }
      return NativeConversation(store, metric, conversationId)
    }
  }
}
