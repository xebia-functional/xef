package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher

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
  dispatcher: Dispatcher,
  store: VectorStore = LocalVectorStore(embeddings),
  noinline block: suspend Conversation.() -> A
): A = block(Conversation(store, dispatcher))
