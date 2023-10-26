package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

/**
 * Executes a conversation with the given embeddings and vector store.
 *
 * @param embeddings The embeddings used for the conversation.
 * @param store The vector store used for the conversation. Defaults to a local vector store.
 * @param block The block of code representing the conversation logic.
 * @return The result of the conversation execution.
 */
suspend inline fun <A> conversation(
  embeddings: Embeddings,
  store: VectorStore = LocalVectorStore(embeddings),
  metric: Metric = Metric.EMPTY,
  noinline block: suspend Conversation.() -> A
): A = block(Conversation(store, metric))
