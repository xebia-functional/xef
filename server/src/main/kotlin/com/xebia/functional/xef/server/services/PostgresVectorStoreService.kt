package com.xebia.functional.xef.server.services

import ai.xef.Embeddings
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.xebia.functional.xef.store.postgresql.addNewCollection
import com.xebia.functional.xef.store.postgresql.connection
import io.github.oshai.kotlinlogging.KLogger
import javax.sql.DataSource
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class PostgresVectorStoreService(
  private val logger: KLogger,
  private val dataSource: DataSource,
  private val collectionName: String,
  private val vectorSize: Int,
  private val embeddingRequestModel: Embeddings,
  private val preDeleteCollection: Boolean = false,
  private val chunkSize: Int = 400,
  private val distanceStrategy: PGDistanceStrategy = PGDistanceStrategy.Euclidean
) : VectorStoreService() {

  fun addCollection() {
    dataSource.connection {
      // Create collection
      val uuid = UUID.generateUUID()
      update(addNewCollection) {
          bind(uuid.toString())
          bind(collectionName)
        }
        .also { logger.info { "Created collection $collectionName" } }
    }
  }

  override fun getVectorStore(token: String?, org: String?): VectorStore {
    return PGVectorStore(
      vectorSize = vectorSize,
      dataSource = dataSource,
      embeddings = embeddingRequestModel,
      collectionName = collectionName,
      distanceStrategy = distanceStrategy,
      preDeleteCollection = preDeleteCollection,
      chunkSize = chunkSize
    )
  }
}
