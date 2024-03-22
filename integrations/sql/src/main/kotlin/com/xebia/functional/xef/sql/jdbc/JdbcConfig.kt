package com.xebia.functional.xef.sql.jdbc

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel

class JdbcConfig(
  val vendor: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
  val database: String,
  val chatApi: ChatApi,
  val model: OpenAIModel<CreateChatCompletionRequestModel>
) {
  fun toJDBCUrl(): String = "jdbc:$vendor://$host:$port/$database"
}
