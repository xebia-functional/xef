package com.xebia.functional.xef.sql.jdbc

import com.xebia.functional.xef.llm.Chat

class JdbcConfig(
  val vendor: String,
  val host: String,
  val username: String,
  val password: String,
  val port: Int,
  val database: String,
  val model: Chat
) {
  fun toJDBCUrl(): String = "jdbc:$vendor://$host:$port/$database"
}
