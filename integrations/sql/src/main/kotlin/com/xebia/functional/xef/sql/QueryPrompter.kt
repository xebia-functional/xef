package com.xebia.functional.xef.sql

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

interface QueryPrompter {
    companion object {
        suspend fun <A> fromJdbcConfig(config: JdbcConfig, block: suspend QueryPrompter.() -> A): A =
            block(QueryPrompterImpl(config))
    }

    @AiDsl
    suspend fun Conversation.query(conversation: String, tables: List<String>, context: String?): String
}

class QueryPrompterImpl(private val config: JdbcConfig) : QueryPrompter {
    private val logger = KotlinLogging.logger {}

    override suspend fun Conversation.query(conversation: String, tables: List<String>, context: String?): String {
        Database.connect(url = config.toJDBCUrl(), user = config.username, password = config.password)
        val tables = transaction { SchemaUtils.listTables() }

        return config.model.promptMessage(
            Prompt(
                """
                 |You are an AI assistant which selects the best tables from which the `goal` can be accomplished.
                 |Select from this list of SQL `tables` the tables that you may need to solve the following `goal`
                 |```tables
                 |$tables
                 |```
                 |```goal
                 |$conversation
                 |```
                 |Instructions:
                 |1. Select the table that you think is the best to solve the `goal`.
                 |2. The tables should be selected from the list of tables above.
                 |3. The tables should be selected by their name.
                 |4. Your response should include a list of tables separated by a comma.
                """.trimIndent()
            )
        )
    }
}