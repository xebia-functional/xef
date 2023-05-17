package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding

class CombinedVectorStore(
    private val top: VectorStore,
    private val bottom: VectorStore
): VectorStore by top {

    fun pop(): VectorStore = bottom

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
