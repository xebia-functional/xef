package com.xebia.functional.xef.store.config

import kotlinx.serialization.Serializable

@Serializable
data class PostgreSQLGraphStoreConfig(
  val url: String,
  val driver: String,
  val user: String,
  val password: String,
  val migrationsTable: String = "migrations",
  val migrationsLocations: List<String> = listOf("graphStore/migrations")
)
