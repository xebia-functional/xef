package com.xebia.functional.xef.server.db.psql

import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult

suspend fun runDatabaseMigration(
  config: XefDatabaseConfig,
): MigrateResult =
  withContext(Dispatchers.IO) {
    val url = config.getUrl()
    val migration: FluentConfiguration =
      Flyway.configure()
        .dataSource(url, config.user, config.password)
        .table(config.migrationsTable)
        .locations(*config.migrationsLocations.toTypedArray())
        .loggers("slf4j")
    val isValid = migration.ignoreMigrationPatterns("*:pending").load().validateWithResult()
    if (!isValid.validationSuccessful) {
      throw IllegalStateException("Migration validation failed: ${isValid.errorDetails}")
    }
    migration.load().migrate()
  }

suspend fun runDatabaseMigration(
  dataSource: DataSource,
  migrationsTable: String,
  migrationsLocations: List<String>,
): MigrateResult =
  withContext(Dispatchers.IO) {
    val migration: FluentConfiguration =
      Flyway.configure()
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
