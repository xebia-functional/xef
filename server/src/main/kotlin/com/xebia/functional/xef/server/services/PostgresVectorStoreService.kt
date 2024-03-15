package com.xebia.functional.xef.server.services

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateEmbeddingRequestModel
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.llm.fromToken
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
  private val distanceStrategy: PGDistanceStrategy = PGDistanceStrategy.Euclidean,
  private val embeddingRequestModel: OpenAIModel<CreateEmbeddingRequestModel> =
    StandardModel(CreateEmbeddingRequestModel.text_embedding_ada_002)
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

  override fun getVectorStore(provider: Provider, token: String?, org: String?): VectorStore {
    val embeddingsApi =
      token?.let { fromToken(token, org) { baseUrl, org -> EmbeddingsApi(baseUrl, org) } }
        ?: fromEnvironment { baseUrl, org -> EmbeddingsApi(baseUrl, org) }

    return PGVectorStore(
      vectorSize = vectorSize,
      dataSource = dataSource,
      embeddings = embeddingsApi,
      collectionName = collectionName,
      distanceStrategy = distanceStrategy,
      preDeleteCollection = preDeleteCollection,
      embeddingRequestModel = embeddingRequestModel,
      chunkSize = chunkSize
    )
  }
}
