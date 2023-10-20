package com.xebia.functional.xef.conversation.sql

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.sql.QueryPrompter
import com.xebia.functional.xef.sql.jdbc.JdbcConfig

val postgres = JdbcConfig(
    vendor = System.getenv("XEF_SQL_DB_VENDOR") ?: "postgresql",
    host = System.getenv("XEF_SQL_DB_HOST") ?: "localhost",
    username = System.getenv("XEF_SQL_DB_USER") ?: "admin",
    password = System.getenv("XEF_SQL_DB_PASSWORD") ?: "admin",
    port = System.getenv("XEF_SQL_DB_PORT")?.toInt() ?: 5432,
    database = System.getenv("XEF_SQL_DB_DATABASE") ?: "capidata",
    model = OpenAI().DEFAULT_CHAT
)

suspend fun main() = OpenAI.conversation {
    QueryPrompter.fromJdbcConfig(postgres) {
        println(promptQuery("Give me top 5 transactions most expensive", listOf("transaction"), ""))
    }
}
