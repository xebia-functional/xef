package com.xebia.functional.xef.sql.jdbc

import com.xebia.functional.xef.openapi.Chat
import com.xebia.functional.xef.openapi.CreateChatCompletionRequest

class JdbcConfig(
  val vendor: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
  val database: String,
  val chatApi: Chat,
  val model: CreateChatCompletionRequest.Model
) {
  fun toJDBCUrl(): String = "jdbc:$vendor://$host:$port/$database"
}
