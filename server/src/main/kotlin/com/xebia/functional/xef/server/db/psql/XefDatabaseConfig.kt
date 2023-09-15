package com.xebia.functional.xef.server.db.psql

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon

@Serializable
class XefDatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String,
    val migrationsTable: String,
    val migrationsLocations: List<String>
) {

    fun getUrl(): String = "jdbc:postgresql://$host:$port/$database"

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun load(
            configNamespace: String,
            config: Config? = null
        ): XefDatabaseConfig =
            withContext(Dispatchers.IO) {
                val rawConfig = config ?: ConfigFactory.load().resolve()
                val jdbcConfig = rawConfig.getConfig(configNamespace)
                Hocon.decodeFromConfig(serializer(), jdbcConfig)
            }

    }
}
