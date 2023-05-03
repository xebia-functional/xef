package com.xebia.functional.vectorstores

import com.xebia.functional.Document
import com.xebia.functional.embeddings.Embedding
import kotlinx.uuid.UUID
import kotlin.jvm.JvmInline

@JvmInline
value class DocumentVectorId(val id: UUID)

interface VectorStore {
  /**
   * Add texts to the vector store after running them through the embeddings
   *
   * @param texts list of text to add to the vector store
   * @return a list of IDs from adding the texts to the vector store
   */
  suspend fun addTexts(texts: List<String>): List<DocumentVectorId>

  suspend fun addText(texts: String): List<DocumentVectorId> =
    addTexts(listOf(texts))

  /**
   * Add documents to the vector store after running them through the embeddings
   *
   * @param documents list of Documents to add to the vector store
   * @return a list of IDs from adding the documents to the vector store
   */
  suspend fun addDocuments(documents: List<Document>): List<DocumentVectorId>

  /**
   * Return the docs most similar to the query
   *
   * @param query text to use to search for similar documents
   * @param limit number of documents to return
   * @return a list of Documents most similar to query
   */
  suspend fun similaritySearch(query: String, limit: Int): List<Document>

  /**
   * Return the docs most similar to the embedding
   *
   * @param embedding embedding vector to use to search for similar documents
   * @param limit number of documents to return
   * @return list of Documents most similar to the embedding
   */
  suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<Document>
}
