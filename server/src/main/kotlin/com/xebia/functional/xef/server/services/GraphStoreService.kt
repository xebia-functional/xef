package com.xebia.functional.xef.server.services

import arrow.fx.coroutines.ResourceScope
import com.typesafe.config.Config
import com.xebia.functional.xef.store.GraphStore
import com.xebia.functional.xef.store.config.PostgreSQLGraphStoreConfig
import com.xebia.functional.xef.store.migrations.runDatabaseMigrations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import org.slf4j.Logger

enum class GraphStoreType {
  PSQL,
  LOCAL;

  companion object {
    fun loadFromConfiguration(config: Config): GraphStoreType {
      val name = config.getString("type")
      return entries.firstOrNull { it.name == name } ?: PSQL
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun ResourceScope.graphStoreService(
  configNamespace: String,
  config: Config,
  logger: Logger
): GraphStoreService =
  withContext(Dispatchers.IO) {
    val graphStoreConfig = config.getConfig(configNamespace)

    when (GraphStoreType.loadFromConfiguration(graphStoreConfig)) {
      GraphStoreType.LOCAL -> error("Local graph store not implemented")
      GraphStoreType.PSQL -> {
        val postgresGraphStoreConfig =
          Hocon.decodeFromConfig(PostgreSQLGraphStoreConfig.serializer(), graphStoreConfig)
        val dataSource =
          hikariDataSource(
            postgresGraphStoreConfig.url,
            postgresGraphStoreConfig.user,
            postgresGraphStoreConfig.password
          )
        runDatabaseMigrations(
          dataSource,
          postgresGraphStoreConfig.migrationsTable,
          postgresGraphStoreConfig.migrationsLocations
        )
        PostgresGraphStoreService(
          logger,
          dataSource,
          postgresGraphStoreConfig
          )
         // .also { it.addCollection() }
      }
    }
  }

interface GraphStoreService {
  suspend fun getSchema()
  suspend fun getGraphStore(graphId: String): GraphStore
}
