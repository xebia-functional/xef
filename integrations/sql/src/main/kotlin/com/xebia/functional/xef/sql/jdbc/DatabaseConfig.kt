package com.xebia.functional.xef.sql.jdbc

import com.xebia.functional.tokenizer.ModelType

class DatabaseConfig(
  val vendor: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
  val database: String,
  val llmModelType: ModelType,
) {
  fun toJDBCUrl(): String = "jdbc:$vendor://$host:$port/$database"
}
