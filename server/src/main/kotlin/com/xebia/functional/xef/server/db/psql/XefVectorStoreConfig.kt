package com.xebia.functional.xef.server.db.psql

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.services.PersistenceService
import com.xebia.functional.xef.server.services.PostgreSQLXef
import com.xebia.functional.xef.server.services.PostgresXefService
import com.xebia.functional.xef.vectorstores.migrations.PsqlVectorStoreConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon

enum class XefVectorStoreType {
    PSQL
}

@Serializable
class XefVectorStoreConfig(
    val type: XefVectorStoreType,
    val host: String,
    val port: Int,
    val database: String,
    val driver: String,
    val user: String,
    val password: String
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun load(
            configNamespace: String,
            config: Config? = null
        ): XefVectorStoreConfig =
            withContext(Dispatchers.IO) {
                val rawConfig = config ?: ConfigFactory.load().resolve()
                val jdbcConfig = rawConfig.getConfig(configNamespace)
                val config = Hocon.decodeFromConfig(serializer(), jdbcConfig)
                config.migrate()
                config
            }

        suspend fun XefVectorStoreConfig.getPersistenceService(config: Config): PersistenceService {
            when (this.type) {
                XefVectorStoreType.PSQL -> {
                    return getPsqlPersistenceService(config)
                }
            }
        }

        private suspend fun getPsqlPersistenceService(config: Config): PersistenceService {
            val vectorStoreConfig = XefVectorStoreConfig.load("xef-vector-store", config)
            val pgVectorStoreConfig = PostgreSQLXef.PGVectorStoreConfig(
                dbConfig = PostgreSQLXef.DBConfig(
                    host = vectorStoreConfig.host,
                    port = vectorStoreConfig.port,
                    database = vectorStoreConfig.database,
                    user = vectorStoreConfig.user,
                    password = vectorStoreConfig.password
                )
            )
            return PostgresXefService(pgVectorStoreConfig)
        }

    }

    private suspend fun migrate() {
        when (this.type) {
            XefVectorStoreType.PSQL -> {
                val psqlConfig = this.toPSQLConfig()
                psqlConfig.migrate()
            }
        }
    }

    private fun XefVectorStoreConfig.toPSQLConfig(): PsqlVectorStoreConfig =
        PsqlVectorStoreConfig(
            host = this.host,
            port = this.port,
            database = this.database,
            driver = this.driver,
            user = this.user,
            password = this.password,
            migrationsTable = "migration"
        )
}
