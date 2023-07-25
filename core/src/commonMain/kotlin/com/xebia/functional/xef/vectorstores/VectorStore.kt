package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding
import kotlin.jvm.JvmStatic

interface VectorStore {

  /**
   * Add memories to the vector store.
   *
   * @param memories List of memories to be stored. It's assumed that they are ordered by timestamp.
   */
  suspend fun addMemories(memories: List<Memory>)

  /**
   * Adds two ordered lists by timestamp into a single list ordered by timestamp.
   */
  fun addOrdered(l1: List<Memory>, l2: List<Memory>): List<Memory> {
    return if (l1.isEmpty()) l2
    else if (l2.isEmpty()) l1
    else {
      val (h1, t1) = l1.let { Pair(it.first(), it.drop(1)) }
      val (h2, t2) = l2.let { Pair(it.first(), it.drop(1)) }
      if (h1.timestamp < h2.timestamp) listOf(h1) + addOrdered(t1, l2)
      else listOf(h2) + addOrdered(l1, t2)
    }
  }

  /**
   * Returns the latest [limit] (chronologically ordered) messages stored.
   *
   * @param conversationId identifier of the conversation.
   * @param limit maximum number of messages to retrieve.
   */
  suspend fun memories(conversationId: ConversationId, limit: Int): List<Memory>

  /**
   * Add texts to the vector store after running them through the embeddings
   *
   * @param texts list of text to add to the vector store
   * @return a list of IDs from adding the texts to the vector store
   */
  suspend fun addTexts(texts: List<String>)

  suspend fun addText(texts: String) = addTexts(listOf(texts))

  /**
   * Return the docs most similar to the query
   *
   * @param query text to use to search for similar documents
   * @param limit number of documents to return
   * @return a list of Documents most similar to query
   */
  suspend fun similaritySearch(query: String, limit: Int): List<String>

  /**
   * Return the docs most similar to the embedding
   *
   * @param embedding embedding vector to use to search for similar documents
   * @param limit number of documents to return
   * @return list of Documents most similar to the embedding
   */
  suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String>

  companion object {
    @JvmStatic
    val EMPTY: VectorStore =
      object : VectorStore {

        override suspend fun addMemories(memories: List<Memory>) {}

        override suspend fun memories(conversationId: ConversationId, limit: Int): List<Memory> =
          emptyList()

        override suspend fun addTexts(texts: List<String>) {}

        override suspend fun similaritySearch(query: String, limit: Int): List<String> = emptyList()

        override suspend fun similaritySearchByVector(
          embedding: Embedding,
          limit: Int
        ): List<String> = emptyList()
      }
  }
}
