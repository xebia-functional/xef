package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingModel
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.server.http.routes.Provider
import com.xebia.functional.xef.vectorstores.PGVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
import com.xebia.functional.xef.vectorstores.postgresql.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

abstract class PersistenceService {
    val logger = KotlinLogging.logger {}

    abstract fun initDatabase(): Unit

    abstract fun getVectorStore(
        provider: Provider = Provider.OPENAI,
        token: String
    ): VectorStore
}

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

class PostgresXefService(
    private val config: PGVectorStoreConfig
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

    override fun initDatabase() {
        getDataSource().connection {
            update(addVectorExtension)
            update(createCollections)
            update(createCollectionsTable)
            update(createMemoryTable)
            update(createEmbeddingTable(config.vectorSize))
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
            Provider.OPENAI -> OpenAIEmbeddings(OpenAI(token).DEFAULT_EMBEDDING)
            else -> OpenAIEmbeddings(OpenAI(token).DEFAULT_EMBEDDING)
        }
        val embeddingModel = EmbeddingModel.TEXT_EMBEDDING_ADA_002

        return PGVectorStore(
            vectorSize = config.vectorSize,
            dataSource = getDataSource(),
            embeddings = embeddings,
            collectionName = config.collectionName,
            distanceStrategy = PGDistanceStrategy.Euclidean,
            preDeleteCollection = config.preDeleteCollection,
            requestConfig =
            RequestConfig(
                model = embeddingModel,
                user = RequestConfig.Companion.User("user")
            ),
            chunkSize = config.chunkSize
        )
    }
}
