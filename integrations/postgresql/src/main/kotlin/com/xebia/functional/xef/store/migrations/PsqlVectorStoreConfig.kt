package com.xebia.functional.xef.store.migrations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult

class PsqlVectorStoreConfig(
  val url: String,
  val driver: String,
  val user: String,
  val password: String,
  val migrationsTable: String,
  val migrationsLocations: List<String> = listOf("vectorStore/migrations")
) {
  suspend fun migrate(): MigrateResult =
    withContext(Dispatchers.IO) {
      val migration: FluentConfiguration = Flyway.configure()
        .dataSource(
          url,
          user,
          password
        )
        .table(migrationsTable)
        .locations(*migrationsLocations.toTypedArray())
        .loggers("slf4j")
      val isValid = migration.ignoreMigrationPatterns("*:pending").load().validateWithResult()
      if (!isValid.validationSuccessful) {
        throw IllegalStateException("Migration validation failed: ${isValid.errorDetails}")
      }
      migration.load().migrate()
    }

  companion object {
    operator fun invoke(
      host: String,
      port: Int,
      database: String,
      driver: String,
      user: String,
      password: String,
      migrationsTable: String,
      migrationsLocations: List<String> = listOf("vectorStore/migrations")
    ): PsqlVectorStoreConfig {
      val uri = "jdbc:postgresql://${host}:${port}/${database}"
      return PsqlVectorStoreConfig(uri, driver, user, password, migrationsTable, migrationsLocations)
    }
  }

}
