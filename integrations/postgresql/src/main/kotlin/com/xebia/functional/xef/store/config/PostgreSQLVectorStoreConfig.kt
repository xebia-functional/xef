package com.xebia.functional.xef.store.config

import kotlinx.serialization.Serializable

@Serializable
data class PostgreSQLVectorStoreConfig(
  val url: String,
  val driver: String,
  val user: String,
  val password: String,
  val collectionName: String,
  val vectorSize: Int,
  val migrationsTable: String = "migrations",
  val migrationsLocations: List<String> = listOf("vectorStore/migrations")
)
