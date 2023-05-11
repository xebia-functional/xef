package com.xebia.functional.vectorstores

import arrow.fx.stm.TMap
import arrow.fx.stm.TVar
import arrow.fx.stm.atomically
import com.xebia.functional.Document
import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.llm.openai.EmbeddingModel
import com.xebia.functional.llm.openai.RequestConfig
import kotlin.math.sqrt
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class LocalVectorStore
private constructor(
  private val embeddings: Embeddings,
  private val documents: TVar<List<Document>>,
  private val documentEmbeddings: TMap<Document, Embedding>
) : VectorStore {

  companion object {
    suspend operator fun invoke(embeddings: Embeddings) =
      LocalVectorStore(embeddings, TVar.new(emptyList()), TMap.new())
  }

  private val requestConfig =
    RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user"))

  override suspend fun addTexts(texts: List<String>): List<DocumentVectorId> {
    val embeddingsList =
      embeddings.embedDocuments(texts, chunkSize = null, requestConfig = requestConfig)
    return texts.zip(embeddingsList) { text, embedding ->
      val document = Document(text)
      atomically {
        documents.modify { it + document }
        documentEmbeddings.insert(document, embedding)
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
    atomically { documents.read().mapNotNull { doc -> documentEmbeddings[doc]?.let { doc to it } } }
      .map { (doc, embedding) -> doc to embedding.cosineSimilarity(embedding) }
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
