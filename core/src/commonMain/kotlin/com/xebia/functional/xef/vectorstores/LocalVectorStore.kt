package com.xebia.functional.xef.vectorstores

import arrow.atomic.Atomic
import arrow.atomic.getAndUpdate
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingModel
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import kotlin.math.sqrt

private data class State(
  val documents: List<String>,
  val precomputedEmbeddings: Map<String, Embedding>
) {
  companion object {
    fun empty(): State = State(emptyList(), emptyMap())
  }
}

private typealias AtomicState = Atomic<State>

class LocalVectorStore
private constructor(private val embeddings: Embeddings, private val state: AtomicState) :
  VectorStore {
  constructor(embeddings: Embeddings) : this(embeddings, Atomic(State.empty()))

  private val requestConfig =
    RequestConfig(EmbeddingModel.TEXT_EMBEDDING_ADA_002, RequestConfig.Companion.User("user"))

  override suspend fun addTexts(texts: List<String>) {
    val embeddingsList =
      embeddings.embedDocuments(texts, chunkSize = null, requestConfig = requestConfig)
    state.getAndUpdate { prevState ->
      val newEmbeddings = prevState.precomputedEmbeddings + texts.zip(embeddingsList)
      State(prevState.documents + texts, newEmbeddings)
    }
  }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> {
    val queryEmbedding = embeddings.embedQuery(query, requestConfig = requestConfig).firstOrNull()
    return queryEmbedding?.let { similaritySearchByVector(it, limit) }.orEmpty()
  }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> {
    val state0 = state.get()
    return state0.documents
      .asSequence()
      .mapNotNull { doc -> state0.precomputedEmbeddings[doc]?.let { doc to it } }
      .map { (doc, e) -> doc to embedding.cosineSimilarity(e) }
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
