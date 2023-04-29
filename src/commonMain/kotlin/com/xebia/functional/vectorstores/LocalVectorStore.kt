package com.xebia.functional.vectorstores

import com.xebia.functional.Document
import com.xebia.functional.embeddings.Embedding
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.llm.openai.EmbeddingModel
import com.xebia.functional.llm.openai.RequestConfig
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlin.math.sqrt

class LocalVectorStore(
    private val embeddings: Embeddings,
) : VectorStore {

  private val requestConfig = RequestConfig(
    EmbeddingModel.TextEmbeddingAda002,
    RequestConfig.Companion.User("user")
  )
  private val documents: MutableList<Document> = mutableListOf()
  private val documentEmbeddings: MutableMap<Document, Embedding> = mutableMapOf()

  override suspend fun addTexts(texts: List<String>): List<DocumentVectorId> {
    val embeddingsList = embeddings.embedDocuments(texts, chunkSize = null, requestConfig = requestConfig)
    return texts.zip(embeddingsList).map { (text, embedding) ->
      val document = Document(text)
      documents.add(document)
      documentEmbeddings[document] = embedding
      val id = DocumentVectorId(UUID.generateUUID())
      id
    }
  }

  override suspend fun addDocuments(documents: List<Document>): List<DocumentVectorId> {
    return addTexts(documents.map(Document::content))
  }

  override suspend fun similaritySearch(query: String, limit: Int): List<Document> {
    val queryEmbedding = embeddings.embedQuery(query, requestConfig = requestConfig).firstOrNull()
    return queryEmbedding?.let {
      similaritySearchByVector(it, limit)
    } ?: emptyList()
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<Document> {
    return documents
        .asSequence()
        .map { document -> document to documentEmbeddings[document]?.cosineSimilarity(embedding) }
      .filter { (_, similarity) -> similarity != null }
      .sortedByDescending { (_, similarity) -> similarity }
      .take(limit)
      .map { (document, _) -> document }
        .toList()
  }

  private fun Embedding.cosineSimilarity(other: Embedding): Double {
    val dotProduct = this.data.zip(other.data).sumOf { (a, b) -> (a * b).toDouble() }
    val magnitudeA = sqrt(this.data.sumOf { (it * it).toDouble() })
    val magnitudeB = sqrt(other.data.sumOf { (it * it).toDouble() })
    return dotProduct / (magnitudeA * magnitudeB)
  }
}
