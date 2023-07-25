package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding

/**
 * A way of composing two [VectorStore] instances together, using [top] for storage by default.
 */
class CombinedVectorStore(private val top: VectorStore, private val bottom: VectorStore) :
  VectorStore by top {
  override suspend fun memories(conversationId: ConversationId, limit: Int): List<Memory> {
    val topResults = top.memories(conversationId, limit)
    val bottomResults = bottom.memories(conversationId, limit)
    return addOrdered(topResults, bottomResults).takeLast(limit)
  }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> {
    val topResults = top.similaritySearch(query, limit)
    return when {
      topResults.size >= limit -> topResults
      else -> topResults + bottom.similaritySearch(query, limit - topResults.size)
    }
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> {
    val topResults = top.similaritySearchByVector(embedding, limit)
    return when {
      topResults.size >= limit -> topResults
      else -> topResults + bottom.similaritySearchByVector(embedding, limit - topResults.size)
    }
  }
}
