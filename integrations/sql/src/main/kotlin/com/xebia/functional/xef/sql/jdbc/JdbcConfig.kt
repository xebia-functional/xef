package com.xebia.functional.xef.sql.jdbc

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel

class JdbcConfig(
  val vendor: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
  val database: String,
  val chatApi: Chat,
  val model: CreateChatCompletionRequestModel
) {
  fun toJDBCUrl(): String = "jdbc:$vendor://$host:$port/$database"
}
