package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.embeddings.EmbeddingsService
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.store.postgresql.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

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


class PostgresXefService(
    private val config: PostgreSQLXef.PGVectorStoreConfig
) : PersistenceService() {

    private fun getDataSource(): HikariDataSource =
        autoClose {
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl =
                        "jdbc:postgresql://${config.dbConfig.host}:${config.dbConfig.port}/${config.dbConfig.database}"
                    username = config.dbConfig.user
                    password = config.dbConfig.password
                    driverClassName = "org.postgresql.Driver"
                }
            )
        }

    override fun addCollection() {
        getDataSource().connection {
            // Create collection
            val uuid = UUID.generateUUID()
            update(addNewCollection) {
                bind(uuid.toString())
                bind(config.collectionName)
            }
        }
    }

    override fun getVectorStore(
        provider: Provider,
        token: String
    ): VectorStore {
        val embeddings = when (provider) {
            Provider.OPENAI -> EmbeddingsService(OpenAI(token).DEFAULT_EMBEDDING, chunkSize = config.chunkSize ?: 400)
            else -> EmbeddingsService(OpenAI(token).DEFAULT_EMBEDDING, chunkSize = config.chunkSize ?: 400)
        }

        return PGVectorStore(
            vectorSize = config.vectorSize,
            dataSource = getDataSource(),
            embeddings = embeddings,
            collectionName = config.collectionName,
            distanceStrategy = PGDistanceStrategy.Euclidean,
            preDeleteCollection = config.preDeleteCollection,
            requestConfig =
            RequestConfig(
                user = RequestConfig.Companion.User("user")
            )
        )
    }
}
