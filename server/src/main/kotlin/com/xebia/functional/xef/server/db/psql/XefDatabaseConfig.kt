package com.xebia.functional.xef.server.db.psql

import com.typesafe.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon

@Serializable
data class XefDatabaseConfig(
  val url: String,
  val user: String,
  val password: String,
  val migrationsTable: String,
  val migrationsLocations: List<String>
) {
  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun load(configNamespace: String, config: Config): XefDatabaseConfig =
      withContext(Dispatchers.IO) {
        val databaseConfig = config.getConfig(configNamespace)
        Hocon.decodeFromConfig(serializer(), databaseConfig)
      }
  }
}
