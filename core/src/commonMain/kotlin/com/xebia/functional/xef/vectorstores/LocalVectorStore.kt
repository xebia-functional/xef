package com.xebia.functional.xef.vectorstores

import arrow.atomic.Atomic
import arrow.atomic.getAndUpdate
import com.aallam.openai.api.ExperimentalOpenAI
import com.aallam.openai.api.embedding.Embedding as AllamEmbedding
import com.aallam.openai.client.extension.distance
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingModel
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

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
    fun toAllam(e: Embedding): AllamEmbedding = AllamEmbedding(e.data.map { it.toDouble() }, 0)
    val target = toAllam(embedding)
    @OptIn(ExperimentalOpenAI::class)
    fun distanceTo(e: Embedding): Double = target.distance(toAllam(e))

    val state0 = state.get()
    return state0.documents
      .asSequence()
      .mapNotNull { doc -> state0.precomputedEmbeddings[doc]?.let { doc to distanceTo(it) } }
      .sortedBy { it.second }
      .take(limit)
      .map { it.first }
      .toList()
  }
}
