package com.xebia.functional.xef.store

import arrow.atomic.AtomicInt
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.store.postgresql.*
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
  private val requestConfig: RequestConfig,
  private val chunkSize: Int?
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
          bind(memory.content.role.name.lowercase())
          bind(memory.content.content)
          bind(memory.index)
        }
      }
    }
  }

  override suspend fun memories(llm: LLM, conversationId: ConversationId, limitTokens: Int): List<Memory> =
    getMemoryByConversationId(conversationId).reduceByLimitToken(llm, limitTokens).reversed()

  override suspend fun systemMessage(conversationId: ConversationId): Memory? =
    getMemoryByConversationId(conversationId).firstOrNull { it.content.role == Role.SYSTEM }

  private fun JDBCSyntax.getCollection(collectionName: String): PGCollection =
    queryOneOrNull(getCollection, { bind(collectionName) }) {
      PGCollection(UUID(string()), string())
    }
      ?: throw IllegalStateException("Collection '$collectionName' not found")

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
      val embeddings = embeddings.embedDocuments(texts, requestConfig, chunkSize)
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
      val embeddings =
        embeddings.embedQuery(query, requestConfig).ifEmpty {
          throw IllegalStateException(
            "Embedding for text: '$query', has not been properly generated"
          )
        }
      val collection = getCollection(collectionName)
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
          content = Message(
            role = Role.valueOf(role.uppercase()),
            content = content,
            name = role,
          ),
          index = index
        )
      }
    }

}
