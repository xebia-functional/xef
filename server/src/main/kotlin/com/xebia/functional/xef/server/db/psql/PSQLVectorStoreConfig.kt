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
  val url: String,
  val driver: String,
  val user: String,
  val password: String,
  val collectionName: String,
  val vectorSize: Int
) : VectorStoreConfig {

  override suspend fun getVectorStoreService(logger: Logger): PostgresVectorStoreService {
    val vectorStoreHikariDataSource = RepositoryService.getHikariDataSource(url, user, password)
    return PostgresVectorStoreService(toPGVectorStoreConfig(), logger, vectorStoreHikariDataSource)
  }

  private fun toPGVectorStoreConfig() =
    PostgreSQLXef.PGVectorStoreConfig(
      dbConfig = PostgreSQLXef.DBConfig(url = url, user = user, password = password),
      collectionName = collectionName,
      vectorSize = vectorSize
    )

  companion object {
    operator fun invoke(
      host: String,
      port: Int,
      database: String,
      driver: String,
      user: String,
      password: String,
      collectionName: String,
      vectorSize: Int
    ): PSQLVectorStoreConfig {
      val url = "jdbc:postgresql://${host}:${port}/${database}"
      return PSQLVectorStoreConfig(url, driver, user, password, collectionName, vectorSize)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun load(configNamespace: String, config: Config?): PSQLVectorStoreConfig =
      withContext(Dispatchers.IO) {
        val rawConfig = config ?: ConfigFactory.load().resolve()
        val jdbcConfig = rawConfig.getConfig(configNamespace)
        val psqlConfig = Hocon.decodeFromConfig(serializer(), jdbcConfig)
        psqlConfig.toPSQLConfig().migrate()
        psqlConfig
      }

    private fun PSQLVectorStoreConfig.toPSQLConfig(): PsqlVectorStoreConfig =
      PsqlVectorStoreConfig(
        url = this.url,
        driver = this.driver,
        user = this.user,
        password = this.password,
        migrationsTable = "migration"
      )
  }
}
