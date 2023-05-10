package com.xebia.functional.vectorstores

import com.xebia.functional.Document
import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.llm.openai.EmbeddingModel
import com.xebia.functional.llm.openai.RequestConfig
import kotlin.math.sqrt
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class LocalVectorStore(private val embeddings: Embeddings) : VectorStore {

  private val requestConfig =
    RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user"))

  private val mutex: Mutex = Mutex()
  private val documents: MutableList<Document> = mutableListOf()
  private val documentEmbeddings: MutableMap<Document, Embedding> = mutableMapOf()

  override suspend fun addTexts(texts: List<String>): List<DocumentVectorId> {
    val embeddingsList =
      embeddings.embedDocuments(texts, chunkSize = null, requestConfig = requestConfig)
    return texts.zip(embeddingsList) { text, embedding ->
      val document = Document(text)
      mutex.withLock {
        documents.add(document)
        documentEmbeddings[document] = embedding
      }
      DocumentVectorId(UUID.generateUUID())
    }
  }

  override suspend fun addDocuments(documents: List<Document>): List<DocumentVectorId> =
    addTexts(documents.map(Document::content))

  override suspend fun similaritySearch(query: String, limit: Int): List<Document> {
    val queryEmbedding = embeddings.embedQuery(query, requestConfig = requestConfig).firstOrNull()
    return queryEmbedding?.let { similaritySearchByVector(it, limit) }.orEmpty()
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<Document> =
    documents
      .mapNotNull { document ->
        mutex
          .withLock { documentEmbeddings[document]?.cosineSimilarity(embedding) }
          ?.let { document to it }
      }
      .sortedByDescending { (_, similarity) -> similarity }
      .take(limit)
      .map { (document, _) -> document }

  private fun Embedding.cosineSimilarity(other: Embedding): Double {
    val dotProduct = this.data.zip(other.data).sumOf { (a, b) -> (a * b).toDouble() }
    val magnitudeA = sqrt(this.data.sumOf { (it * it).toDouble() })
    val magnitudeB = sqrt(other.data.sumOf { (it * it).toDouble() })
    return dotProduct / (magnitudeA * magnitudeB)
  }
}
