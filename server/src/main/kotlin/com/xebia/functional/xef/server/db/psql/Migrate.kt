package com.xebia.functional.xef.server.db.psql

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult

object Migrate {
    suspend fun migrate(
        config: XefDatabaseConfig,
    ): MigrateResult =
        withContext(Dispatchers.IO) {
            val url = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
            val migration: FluentConfiguration = Flyway.configure()
                .dataSource(
                    url,
                    config.user,
                    config.password
                )
                .table(config.migrationsTable)
                .locations(*config.migrationsLocations.toTypedArray())
                .loggers("slf4j")
            val isValid = migration.ignoreMigrationPatterns("*:pending").load().validateWithResult()
            if (!isValid.validationSuccessful) {
                throw IllegalStateException("Migration validation failed: ${isValid.errorDetails}")
            }
            migration.load().migrate()
        }
}
