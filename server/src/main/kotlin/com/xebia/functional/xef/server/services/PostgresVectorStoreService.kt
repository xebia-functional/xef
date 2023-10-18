package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.xebia.functional.xef.store.postgresql.addNewCollection
import com.xebia.functional.xef.store.postgresql.connection
import com.zaxxer.hikari.HikariDataSource
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.slf4j.Logger

object PostgreSQLXef {
  data class DBConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
  )

  data class PGVectorStoreConfig(
    val dbConfig: DBConfig,
    val vectorSize: Int = 3,
    val collectionName: String = "xef_collection",
    val preDeleteCollection: Boolean = false,
    val chunkSize: Int? = null,
  )
}

class PostgresVectorStoreService(
  private val config: PostgreSQLXef.PGVectorStoreConfig,
  private val logger: Logger,
  private val dataSource: HikariDataSource
) : VectorStoreService() {

  override fun addCollection() {
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

  override fun getVectorStore(provider: Provider, token: String): VectorStore {
    val embeddings =
      when (provider) {
        Provider.OPENAI -> OpenAI(token).DEFAULT_EMBEDDING
        else -> OpenAI(token).DEFAULT_EMBEDDING
      }

    return PGVectorStore(
      vectorSize = config.vectorSize,
      dataSource = dataSource,
      embeddings = embeddings,
      collectionName = config.collectionName,
      distanceStrategy = PGDistanceStrategy.Euclidean,
      preDeleteCollection = config.preDeleteCollection,
      requestConfig = RequestConfig(user = RequestConfig.Companion.User("user")),
      chunkSize = config.chunkSize
    )
  }
}
