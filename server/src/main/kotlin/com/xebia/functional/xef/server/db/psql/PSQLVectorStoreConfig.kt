package com.xebia.functional.xef.server.db.psql

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.db.VectorStoreConfig
import com.xebia.functional.xef.server.services.PostgreSQLXef
import com.xebia.functional.xef.server.services.PostgresVectorStoreService
import com.xebia.functional.xef.server.services.RepositoryService
import com.xebia.functional.xef.store.migrations.PsqlVectorStoreConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import org.slf4j.Logger

@Serializable
class PSQLVectorStoreConfig(
  val host: String,
  val port: Int,
  val database: String,
  val driver: String,
  val user: String,
  val password: String,
  val collectionName: String,
  val vectorSize: Int
) : VectorStoreConfig {

  fun getUrl(): String = "jdbc:postgresql://$host:$port/$database"

  override suspend fun getVectorStoreService(logger: Logger): PostgresVectorStoreService {
    val vectorStoreHikariDataSource =
      RepositoryService.getHikariDataSource(getUrl(), user, password)
    return PostgresVectorStoreService(toPGVectorStoreConfig(), logger, vectorStoreHikariDataSource)
  }

  private fun toPGVectorStoreConfig() =
    PostgreSQLXef.PGVectorStoreConfig(
      dbConfig =
        PostgreSQLXef.DBConfig(
          host = host,
          port = port,
          database = database,
          user = user,
          password = password
        ),
      collectionName = collectionName,
      vectorSize = vectorSize
    )

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun load(configNamespace: String, config: Config?): PSQLVectorStoreConfig =
      withContext(Dispatchers.IO) {
        val rawConfig = config ?: ConfigFactory.load().resolve()
        val jdbcConfig = rawConfig.getConfig(configNamespace)
        val config = Hocon.decodeFromConfig(serializer(), jdbcConfig)
        config.toPSQLConfig().migrate()
        config
      }

    private fun PSQLVectorStoreConfig.toPSQLConfig(): PsqlVectorStoreConfig =
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
}
