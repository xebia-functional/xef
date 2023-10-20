package com.xebia.functional.xef.sql

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.ResultSet

interface QueryPrompter {
    companion object {
        suspend fun <A> fromJdbcConfig(config: JdbcConfig, block: suspend QueryPrompter.() -> A): A =
            block(QueryPrompterImpl(config))
    }

    /**
     * Returns a queryResult found in the database for the given [prompt]
     */
    @AiDsl
    suspend fun Conversation.promptQuery(prompt: String, tables: List<String>, context: String?): QueryResult

    /**
     * Generates SQL from the databases and input prompt
     */
    @AiDsl
    suspend fun Conversation.query(prompt: String, tables: List<String>, columns: String): String
}

class QueryPrompterImpl(private val config: JdbcConfig) : QueryPrompter {
    private val logger = KotlinLogging.logger {}
    override suspend fun Conversation.promptQuery(
        prompt: String, tables: List<String>, context: String?
    ): QueryResult {
        Database.connect(url = config.toJDBCUrl(), user = config.username, password = config.password)
        val ddls = tables.map {
            Pair(it, transaction {
                val query = """select * from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='$it'"""
                TransactionManager.current().connection.prepareStatement(query, false).executeQuery().getColumnName()
            })
        }

        logger.debug { ddls }

        val ctx =
            ddls.joinToString("\n") { "${it.first}: ${it.second.joinToString(",")}" }

        logger.debug { ctx }

        val query = query(prompt, tables, ctx)

        logger.debug { "query: $query" }

        return transaction {
            TransactionManager.current().connection.prepareStatement(query, false).executeQuery().toQueryResult()

        }
    }

    override suspend fun Conversation.query(prompt: String, tables: List<String>, columns: String): String {
        return config.model.promptMessage(
            Prompt(
                """
                 |You are an AI assistant which selects the best tables from which the `goal` can be accomplished 
                 |and generates the SQL query.
                 |Select from this list of SQL `tables` the tables that you may need to solve the following `goal`.
                 |Use the `columns` to have more information about the columns of the table to answer properly.
                 | 
                 |```tables
                 |$tables
                 |```
                 |```columns
                 |$columns
                 |```
                 |```goal
                 |$prompt
                 |```
                 |Instructions:
                 |1. Select the tables that you think is the best to solve the `goal`.
                 |2. The tables should be selected from the list of tables above.
                 |3. Generate the SQL query.
                 |4. Your response should be only the SQL query.
                """.trimIndent()
            )
        )
    }

    /**
     * Converts a JDBC ResultSet into a QueryResult
     */
    private fun ResultSet.toQueryResult(): QueryResult {
        val columns = mutableListOf<Column>()
        val rows = mutableListOf<List<String?>>()

        logger.debug { "nº columns ${metaData.columnCount}" }

        for (i in 1..metaData.columnCount) {
            val fieldName = metaData.getColumnName(i)
            val fieldType = metaData.getColumnTypeName(i)
            columns.add(Column(fieldName, fieldType))
        }

        while (next()) {
            val row = mutableListOf<String?>()
            for (i in 1..metaData.columnCount) row.add(getString(i))
            rows.add(row)
        }

        return QueryResult(columns, rows)
    }

    private fun ResultSet.getColumnName(): List<String> {
        val rows = mutableListOf<String>()
        while (next()) {
            rows.add(getString("column_name"))
        }
        return rows
    }
}