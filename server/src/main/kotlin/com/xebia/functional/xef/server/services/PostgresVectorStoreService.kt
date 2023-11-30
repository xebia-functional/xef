package com.xebia.functional.xef.server.services

import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.xebia.functional.xef.store.postgresql.addNewCollection
import com.xebia.functional.xef.store.postgresql.connection
import javax.sql.DataSource
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.slf4j.Logger

class PostgresVectorStoreService(
  private val logger: Logger,
  private val dataSource: DataSource,
  private val collectionName: String,
  private val vectorSize: Int,
  private val preDeleteCollection: Boolean = false,
  private val chunkSize: Int = 400,
) : VectorStoreService() {

  fun addCollection() {
    dataSource.connection {
      // Create collection
      val uuid = UUID.generateUUID()
      update(addNewCollection) {
          bind(uuid.toString())
          bind(collectionName)
        }
        .also { logger.info("Created collection $collectionName") }
    }
  }

  override fun getVectorStore(provider: Provider, token: String?): VectorStore {
    val embeddingsApi = fromEnvironment { baseUrl -> EmbeddingsApi(baseUrl) }
    if (token != null) {
      embeddingsApi.setApiKey(token)
    }

    // TODO - Provider?
    //    val embeddings =
    //      when (provider) {
    //        Provider.OPENAI -> openAI.DEFAULT_EMBEDDING
    //        else -> openAI.DEFAULT_EMBEDDING
    //      }

    return PGVectorStore(
      vectorSize = vectorSize,
      dataSource = dataSource,
      embeddings = embeddingsApi,
      collectionName = collectionName,
      distanceStrategy = PGDistanceStrategy.Euclidean,
      preDeleteCollection = preDeleteCollection,
      chunkSize = chunkSize
    )
  }
}
