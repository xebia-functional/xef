package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class Conversation
@JvmOverloads
constructor(
  val store: VectorStore = VectorStore.EMPTY,
  val metric: Metric = Metric.EMPTY,
  val conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
) {

  companion object {

    @JvmSynthetic
    suspend operator fun <A> invoke(
      store: VectorStore = VectorStore.EMPTY,
      metric: Metric = Metric.EMPTY,
      conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString()),
      block: suspend Conversation.() -> A
    ): A = block(Conversation(store, metric, conversationId))
  }
}
