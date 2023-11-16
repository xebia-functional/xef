package com.xebia.functional.xef.store

import ai.xef.openai.OpenAIModel
import arrow.atomic.Atomic
import arrow.atomic.AtomicInt
import arrow.atomic.getAndUpdate
import arrow.atomic.update
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.Embedding
import com.xebia.functional.xef.llm.embedDocuments
import com.xebia.functional.xef.llm.embedQuery
import kotlin.math.sqrt

private data class State(
  val orderedMemories: Map<ConversationId, List<Memory>>,
  val documents: List<String>,
  val precomputedEmbeddings: Map<String, Embedding>
) {
  companion object {
    fun empty(): State = State(emptyMap(), emptyList(), emptyMap())
  }
}

private typealias AtomicState = Atomic<State>

class LocalVectorStore
private constructor(private val embeddings: EmbeddingsApi, private val state: AtomicState) :
  VectorStore {
  constructor(embeddings: EmbeddingsApi) : this(embeddings, Atomic(State.empty()))

  override val indexValue: AtomicInt = AtomicInt(0)

  override fun updateIndexByConversationId(conversationId: ConversationId) {
    state.get().orderedMemories[conversationId]?.let { memories ->
      memories.maxByOrNull { it.index }?.let { lastMemory -> indexValue.set(lastMemory.index) }
    }
  }

  override suspend fun addMemories(memories: List<Memory>) {
    state.update { prevState ->
      prevState.copy(
        orderedMemories =
          memories
            .groupBy { it.conversationId }
            .let { memories ->
              (prevState.orderedMemories.keys + memories.keys).associateWith { key ->
                val l1 = prevState.orderedMemories[key] ?: emptyList()
                val l2 = memories[key] ?: emptyList()
                l1 + l2
              }
            }
      )
    }
  }

  override suspend fun <T> memories(
    model: OpenAIModel<T>,
    conversationId: ConversationId,
    limitTokens: Int
  ): List<Memory> {
    val memories = state.get().orderedMemories[conversationId]
    return memories
      .orEmpty()
      .sortedByDescending { it.index }
      .reduceByLimitToken(model.modelType(), limitTokens)
      .reversed()
  }

  override suspend fun addTexts(texts: List<String>) {
    val embeddingsList = embeddings.embedDocuments(texts, null)
    state.getAndUpdate { prevState ->
      val newEmbeddings = prevState.precomputedEmbeddings + texts.zip(embeddingsList)
      State(prevState.orderedMemories, prevState.documents + texts, newEmbeddings)
    }
  }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> {
    val queryEmbedding = embeddings.embedQuery(query).firstOrNull()
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
    val dotProduct = this.embedding.zip(other.embedding).sumOf { (a, b) -> (a * b).toDouble() }
    val magnitudeA = sqrt(this.embedding.sumOf { (it * it).toDouble() })
    val magnitudeB = sqrt(other.embedding.sumOf { (it * it).toDouble() })
    return dotProduct / (magnitudeA * magnitudeB)
  }
}
