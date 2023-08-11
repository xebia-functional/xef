package com.xebia.functional.xef.server.db.psql

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.services.DBConfig
import com.xebia.functional.xef.server.services.PGVectorStoreConfig
import com.xebia.functional.xef.server.services.PersistenceService
import com.xebia.functional.xef.server.services.PostgresXefService
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
    val password: String,
    val vectorSize: Int
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
                Hocon.decodeFromConfig(serializer(), jdbcConfig)
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
            val pgVectorStoreConfig = PGVectorStoreConfig(
                dbConfig = DBConfig(
                    host = vectorStoreConfig.host,
                    port = vectorStoreConfig.port,
                    database = vectorStoreConfig.database,
                    user = vectorStoreConfig.user,
                    password = vectorStoreConfig.password
                ),
                vectorSize = vectorStoreConfig.vectorSize
            )
            return PostgresXefService(pgVectorStoreConfig)
        }

    }
}
