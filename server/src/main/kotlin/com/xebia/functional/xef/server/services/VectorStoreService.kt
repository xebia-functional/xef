package com.xebia.functional.xef.server.services

import arrow.fx.coroutines.ResourceScope
import com.typesafe.config.Config
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.store.config.PostgreSQLVectorStoreConfig
import com.xebia.functional.xef.store.migrations.runDatabaseMigrations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import org.slf4j.Logger

enum class XefVectorStoreType {
  PSQL,
  LOCAL;

  companion object {
    fun loadFromConfiguration(config: Config): XefVectorStoreType {
      val name = config.getString("type")
      return entries.firstOrNull { it.name == name } ?: PSQL
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun ResourceScope.vectorStoreService(
  configNamespace: String,
  config: Config,
  logger: Logger
): VectorStoreService =
  withContext(Dispatchers.IO) {
    val vectorStoreConfig = config.getConfig(configNamespace)

    when (XefVectorStoreType.loadFromConfiguration(vectorStoreConfig)) {
      XefVectorStoreType.LOCAL -> LocalVectorStoreService()
      XefVectorStoreType.PSQL -> {
        val postgresVectorStoreConfig =
          Hocon.decodeFromConfig(PostgreSQLVectorStoreConfig.serializer(), vectorStoreConfig)
        val dataSource =
          hikariDataSource(
            postgresVectorStoreConfig.url,
            postgresVectorStoreConfig.user,
            postgresVectorStoreConfig.password
          )
        runDatabaseMigrations(
          dataSource,
          postgresVectorStoreConfig.migrationsTable,
          postgresVectorStoreConfig.migrationsLocations
        )
        PostgresVectorStoreService(
            logger,
            dataSource,
            postgresVectorStoreConfig.collectionName,
            postgresVectorStoreConfig.vectorSize
          )
          .also { it.addCollection() }
      }
    }
  }

abstract class VectorStoreService {
  abstract fun getVectorStore(token: String? = null, org: String? = null): VectorStore
}
