package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.Embedding
import com.xebia.functional.xef.Config
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable

interface VectorStore {

  @Serializable
  data class Document(val content: String, val source: String) {
    fun toJson(): String = Config.DEFAULT.json.encodeToString(serializer(), this)

    companion object {
      fun fromJson(json: String): Document =
        Config.DEFAULT.json.decodeFromString(serializer(), json)
    }
  }

  val indexValue: AtomicInt

  fun incrementIndexAndGet(): Int = indexValue.addAndGet(1)

  fun updateIndexByConversationId(conversationId: ConversationId)

  suspend fun addMemories(memories: List<Memory>)

  suspend fun memories(
    model: CreateChatCompletionRequestModel,
    conversationId: ConversationId,
    limitTokens: Int
  ): List<Memory>

  /**
   * Add texts to the vector store after running them through the embeddings
   *
   * @param texts list of text to add to the vector store
   * @return a list of IDs from adding the texts to the vector store
   */
  suspend fun addDocuments(texts: List<Document>)

  suspend fun addDocument(texts: Document) = addDocuments(listOf(texts))

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

  companion object {
    @JvmStatic
    val EMPTY: VectorStore =
      object : VectorStore {
        override val indexValue: AtomicInt = AtomicInt(0)

        override fun updateIndexByConversationId(conversationId: ConversationId) {}

        override suspend fun addMemories(memories: List<Memory>) {}

        override suspend fun memories(
          model: CreateChatCompletionRequestModel,
          conversationId: ConversationId,
          limitTokens: Int
        ): List<Memory> = emptyList()

        override suspend fun addDocuments(texts: List<Document>) {}

        override suspend fun similaritySearch(query: String, limit: Int): List<Document> =
          emptyList()

        override suspend fun similaritySearchByVector(
          embedding: Embedding,
          limit: Int
        ): List<Document> = emptyList()
      }
  }
}
