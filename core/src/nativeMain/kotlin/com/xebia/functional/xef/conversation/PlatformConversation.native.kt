package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.Memory
import com.xebia.functional.xef.store.VectorStore

actual abstract class PlatformConversation
actual constructor(store: VectorStore, conversationId: ConversationId?) :
  Conversation, AutoClose, AutoCloseable {
  actual companion object {
    actual suspend fun create(
      store: VectorStore,
      metric: Metric,
      conversationId: ConversationId?,
      system: String?,
    ): PlatformConversation {
      conversationId?.let { cid ->
        store.updateIndexByConversationId(cid)
        if (
          system != null && store.systemMessage(cid) == null
        ) { // only if system message is not already present
          store.addMemories(
            listOf(
              Memory(
                cid,
                Message(Role.SYSTEM, system, Role.SYSTEM.name),
                store.incrementIndexAndGet()
              )
            )
          )
        }
      }
      return NativeConversation(store, metric, conversationId)
    }
  }
}
