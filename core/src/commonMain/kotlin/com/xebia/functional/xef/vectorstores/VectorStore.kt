package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.embeddings.Embedding
import kotlin.jvm.JvmStatic

interface VectorStore : AutoCloseable {
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

  override fun close() {
    // no-op
  }

  companion object {
    @JvmStatic
    val EMPTY: VectorStore =
      object : VectorStore {
        override suspend fun addTexts(texts: List<String>) {}
        override suspend fun similaritySearch(query: String, limit: Int): List<String> = emptyList()
        override suspend fun similaritySearchByVector(
          embedding: Embedding,
          limit: Int
        ): List<String> = emptyList()
      }
  }
}
