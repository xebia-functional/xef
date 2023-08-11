package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding

/**
 * A way of composing two [VectorStore] instances together, this class will **first search** [top],
 * and then [bottom].
 *
 * If all results can be found in [top] it will skip searching [bottom].
 */
class CombinedVectorStore(private val top: VectorStore, private val bottom: VectorStore) :
  VectorStore by top {

  override suspend fun memories(conversationId: ConversationId, limitTokens: Int): List<Memory> {
    val bottomResults = bottom.memories(conversationId, limitTokens)
    val topResults = top.memories(conversationId, limitTokens)

    return (topResults + bottomResults)
      .sortedByDescending { it.timestamp }
      .fold(Pair(0, emptyList<Memory>())) { (accTokens, list), memory ->
        val totalTokens = accTokens + memory.approxTokens
        if (totalTokens <= limitTokens) {
          Pair(totalTokens, list + memory)
        } else {
          Pair(accTokens, list)
        }
      }
      .second
      .reversed()
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
