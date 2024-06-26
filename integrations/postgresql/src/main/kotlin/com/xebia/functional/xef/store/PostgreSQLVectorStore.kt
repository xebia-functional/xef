package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.xef.llm.embedDocuments
import com.xebia.functional.xef.llm.embedQuery
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.prompt.contentAsString
import com.xebia.functional.xef.store.postgresql.*
import com.xebia.functional.xef.openapi.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import javax.sql.DataSource

class PGVectorStore(
  private val vectorSize: Int,
  private val dataSource: DataSource,
  private val embeddings: Embeddings,
  private val collectionName: String,
  private val distanceStrategy: PGDistanceStrategy,
  private val preDeleteCollection: Boolean,
  private val embeddingRequestModel: CreateEmbeddingRequest.Model,
  private val chunkSize: Int = 400
) : VectorStore {

  override val indexValue: AtomicInt = AtomicInt(0)

  override fun updateIndexByConversationId(conversationId: ConversationId) {
    getMemoryByConversationId(conversationId).firstOrNull()?.let { indexValue.set(it.index) }
  }

  override suspend fun addMemories(memories: List<Memory>) {
    dataSource.connection {
      memories.forEach { memory ->
        update(addNewMemory) {
          bind(UUID.generateUUID().toString())
          bind(memory.conversationId.value)
          bind(memory.content.role.name)
          bind(memory.content.asRequestMessage().contentAsString())
          bind(memory.index)
        }
      }
    }
  }

  override suspend fun memories(model: CreateChatCompletionRequest.Model, conversationId: ConversationId, limitTokens: Int): List<Memory> =
    getMemoryByConversationId(conversationId).reduceByLimitToken(model.modelType(), limitTokens).reversed()

  private fun JDBCSyntax.getCollection(collectionName: String): PGCollection =
    queryOneOrNull(getCollection, { bind(collectionName) }) {
      PGCollection(UUID(string()), string())
    }
      ?: throw IllegalStateException("Collection '$collectionName' not found")

  private fun JDBCSyntax.collectionHasContent(collectionId: String): Boolean =
    queryOneOrNull(hasOneEmbeddings, { bind(collectionId) }) { true } ?: false

  private fun JDBCSyntax.deleteCollection() {
    if (preDeleteCollection) {
      val collection = getCollection(collectionName)
      update(deleteCollectionDocs) { bind(collection.uuid.toString()) }
      update(deleteCollection) { bind(collection.uuid.toString()) }
    }
  }

  fun initialDbSetup(): Unit =
    dataSource.connection {
      update(addVectorExtension)
      update(createCollectionsTable)
      update(createMemoryTable)
      update(createEmbeddingTable(vectorSize))
      deleteCollection()
    }

  fun createCollection(): Unit =
    dataSource.connection {
      val uuid = UUID.generateUUID()
      update(addNewCollection) {
        bind(uuid.toString())
        bind(collectionName)
      }
    }

  override suspend fun addTexts(texts: List<String>): Unit =
    dataSource.connection {
      val embeddings = embeddings.embedDocuments(texts, chunkSize, embeddingRequestModel)
      val collection = getCollection(collectionName)
      texts.zip(embeddings) { text, embedding ->
        val uuid = UUID.generateUUID()
        update(addNewText) {
          bind(uuid.toString())
          bind(collection.uuid.toString())
          bind(embedding.embedding.toString())
          bind(text)
        }
      }
    }

  override suspend fun similaritySearch(query: String, limit: Int): List<String> =
    dataSource.connection {
      val collection = getCollection(collectionName)

      val hasEmbeddings = collectionHasContent(collection.uuid.toString())

      if (!hasEmbeddings) return emptyList()

      val embeddings =
        embeddings.embedQuery(query, embeddingRequestModel).ifEmpty {
          throw IllegalStateException(
            "Embedding for text: '$query', has not been properly generated"
          )
        }

      queryAsList(
        searchSimilarDocument(distanceStrategy),
        {
          bind(collection.uuid.toString())
          bind(embeddings[0].embedding.toString())
          bind(limit)
        }
      ) {
        string()
      }
    }

  override suspend fun similaritySearchByVector(embedding: Embedding, limit: Int): List<String> =
    dataSource.connection {
      val collection = getCollection(collectionName)
      queryAsList(
        searchSimilarDocument(distanceStrategy),
        {
          bind(collection.uuid.toString())
          bind(embedding.embedding.toString())
          bind(limit)
        }
      ) {
        string()
      }
    }

  private fun getMemoryByConversationId(conversationId: ConversationId): List<Memory> =
    dataSource.connection {
      queryAsList(getMemoriesByConversationId, {
        bind(conversationId.value)
      }) {
        val uuid = string()
        val cId = string()
        val role = string()
        val content = string()
        val index = int()
        Memory(
          conversationId = ConversationId(cId),
          content = memorizedMessage(ChatCompletionRole.valueOf(role), content),
          index = index
        )
      }
    }

}
