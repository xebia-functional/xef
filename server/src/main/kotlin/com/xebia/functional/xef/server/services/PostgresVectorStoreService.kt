package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.store.config.PostgreSQLVectorStoreConfig
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.xebia.functional.xef.store.postgresql.addNewCollection
import com.xebia.functional.xef.store.postgresql.connection
import javax.sql.DataSource
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.slf4j.Logger

data class PostgresVectorStoreConfig(
  val vectorSize: Int = 1536, // OpenAI default
  val collectionName: String = "xef_collection",
  val preDeleteCollection: Boolean = false,
  val chunkSize: Int? = null,
)

class PostgresVectorStoreService(
  private val config: PostgreSQLVectorStoreConfig,
  private val logger: Logger,
  private val dataSource: DataSource,
  private val preDeleteCollection: Boolean = false,
  private val chunkSize: Int? = null,
) : VectorStoreService() {

  fun addCollection() {
    dataSource.connection {
      // Create collection
      val uuid = UUID.generateUUID()
      update(addNewCollection) {
          bind(uuid.toString())
          bind(config.collectionName)
        }
        .also { logger.info("Created collection ${config.collectionName}") }
    }
  }

  override fun getVectorStore(provider: Provider, token: String?): VectorStore {
    val openAI = if (token == null) OpenAI.fromEnvironment() else OpenAI(token)

    val embeddings =
      when (provider) {
        Provider.OPENAI -> openAI.DEFAULT_EMBEDDING
        else -> openAI.DEFAULT_EMBEDDING
      }

    return PGVectorStore(
      vectorSize = config.vectorSize,
      dataSource = dataSource,
      embeddings = embeddings,
      collectionName = config.collectionName,
      distanceStrategy = PGDistanceStrategy.Euclidean,
      preDeleteCollection = preDeleteCollection,
      requestConfig = RequestConfig(user = RequestConfig.Companion.User("user")),
      chunkSize = chunkSize
    )
  }
}
