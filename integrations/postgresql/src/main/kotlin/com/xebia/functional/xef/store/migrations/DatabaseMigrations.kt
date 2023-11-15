package com.xebia.functional.xef.store.migrations

import com.xebia.functional.xef.store.config.PostgreSQLVectorStoreConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

suspend fun runDatabaseMigrations(
  dataSource: DataSource,
  migrationsTable: String,
  migrationsLocations: List<String>
): MigrateResult =
  withContext(Dispatchers.IO) {
    val migration: FluentConfiguration = Flyway.configure()
      .dataSource(dataSource)
      .table(migrationsTable)
      .locations(*migrationsLocations.toTypedArray())
      .loggers("slf4j")
    val isValid = migration.ignoreMigrationPatterns("*:pending").load().validateWithResult()
    if (!isValid.validationSuccessful) {
      throw IllegalStateException("Migration validation failed: ${isValid.errorDetails}")
    }
    migration.load().migrate()
  }

suspend fun runDatabaseMigrations(
  config: PostgreSQLVectorStoreConfig
): MigrateResult =
  withContext(Dispatchers.IO) {
    with(config) {
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
  }
