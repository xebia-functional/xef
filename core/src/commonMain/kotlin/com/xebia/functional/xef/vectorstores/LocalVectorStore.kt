package com.xebia.functional.xef.vectorstores

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import arrow.fx.stm.TMap
import arrow.fx.stm.TVar
import arrow.fx.stm.atomically
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.openai.EmbeddingModel
import com.xebia.functional.xef.llm.openai.RequestConfig
import kotlin.math.sqrt

val LocalVectorStoreBuilder: (Embeddings) -> Resource<LocalVectorStore> = { e ->
  resource { LocalVectorStore(e) }
}

class LocalVectorStore
private constructor(
  private val embeddings: Embeddings,
  private val documents: TVar<List<String>>,
  private val precomputedEmbeddings: TMap<String, Embedding>
) : VectorStore {

  companion object {
    suspend operator fun invoke(embeddings: Embeddings) =
      LocalVectorStore(embeddings, TVar.new(emptyList()), TMap.new())
  }

  private val requestConfig =
    RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user"))

  override suspend fun addTexts(texts: List<String>) {
    val embeddingsList =
      embeddings.embedDocuments(texts, chunkSize = null, requestConfig = requestConfig)
    texts.zip(embeddingsList) { text, embedding ->
      atomically {
        documents.modify { it + text }
        precomputedEmbeddings.insert(text, embedding)
      }
    }
  }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> {
    val queryEmbedding = embeddings.embedQuery(query, requestConfig = requestConfig).firstOrNull()
    return queryEmbedding?.let { similaritySearchByVector(it, limit) }.orEmpty()
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> =
    atomically {
        documents.read().mapNotNull { doc -> precomputedEmbeddings[doc]?.let { doc to it } }
      }
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
