package com.xebia.functional.xef.store.migrations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.MigrateResult

class PsqlVectorStoreConfig (
    val host: String,
    val port: Int,
    val database: String,
    val driver: String,
    val user: String,
    val password: String,
    val migrationsTable: String,
    val migrationsLocations: List<String> = listOf("vectorStore/migrations")
) {
    suspend fun migrate(): MigrateResult =
        withContext(Dispatchers.IO) {
            val url = "jdbc:postgresql://${host}:${port}/${database}"
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
