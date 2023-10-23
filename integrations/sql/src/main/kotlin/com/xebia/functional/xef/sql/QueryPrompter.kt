package com.xebia.functional.xef.sql

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.Delimiter
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.sql.ResultSetOps.getColumnByName
import com.xebia.functional.xef.sql.ResultSetOps.toQueryResult
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    suspend fun Conversation.query(
        input: String,
        tables: List<String>,
        columns: String,
        context: String?
    ): QueriesAnswer
}

class QueryPrompterImpl(private val config: JdbcConfig) : QueryPrompter {
    private val logger = KotlinLogging.logger {}
    override suspend fun Conversation.promptQuery(
        prompt: String, tables: List<String>, context: String?
    ): QueryResult {
        logger.debug { "[Input]: $prompt" }

        Database.connect(url = config.toJDBCUrl(), user = config.username, password = config.password)

        val columnsPerTable = tables.map {
            Pair(it, transaction {
                val query = "select * from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='$it'"
                TransactionManager.current().connection.prepareStatement(query, false).executeQuery()
                    .getColumnByName("column_name")
            })
        }

        val columnsCtx =
            columnsPerTable.joinToString("\n") { "${it.first}: ${it.second.joinToString(",")}" }

        logger.debug { "[Columns per table]: $columnsCtx" }

        val answer = query(prompt, tables, columnsCtx, context)

        logger.debug { "[answer]: $answer" }

        return transaction {
            TransactionManager.current().connection.prepareStatement(answer.mainQuery, false).executeQuery()
                .toQueryResult()
        }
    }

    override suspend fun Conversation.query(
        input: String,
        tables: List<String>,
        columns: String,
        context: String?
    ): QueriesAnswer {
        val prompt = Prompt {
            +system(
                """
                 |You are an expert in SQL queries which selects the best tables and generates the query.
                 |Select from this list of SQL `tables` the tables that you may need to solve the input.
                 |Keep into account today's date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                 |Use the `columns` to have more information about the fields of the table to answer properly.
                 |Use the `context` to accurate the answer.
                 | 
                 |```tables
                 |$tables
                 |```
                 |```columns
                 |$columns
                 |```
                 |```context
                 |$context
                 |```
                 |ExpectedOutput {
                 |    The expected result need to include 3 fields. These are the criteria to generate all the fields that compose the final result:
                 |    - MainResponse: This is mandatory and it is the SQL that satisfies the input of the user.
                 |    - FriendlyResponse: This is mandatory and this is a friendly sentence that summarize the output. In case that the MainResponse is a query that returns one single item (when the query includes COUNT, MAX, MIN, SUM, AVG, etc.), the friendly sentence can refer that data as XXX, that we can inject once we run the sql query.
                 |    - DetailedResponse: This is an optional field. In case that the MainResponse represents an operation like COUNT, MAX, MIN, AVG, SUM, etc, you have to generate another similar query to show all the transactions involved in the MainResponse.
                 |}
                 |Instructions:
                 |1. Select the tables that you think is the best to solve the input.
                 |2. The tables should be selected from the list of tables above.
                 |3. Analyse the `context`.
                 |4. Generate the SQL query.
                 |5. Finally generate the expected output described in ExpectedOutput, the output has to accomplish the expectations of the user and the output format described above.
                """.trimIndent()
            )
            +user(
                """
                |```input
                |$input
                |```
            """.trimIndent()
            )
        }

        val queriesAnswerSerializer = serializer<QueriesAnswer>()

        return config.model.prompt(
            prompt = prompt,
            scope = this,
            serializer = queriesAnswerSerializer
        )
    }

}

@Serializable
data class QueriesAnswer(
    val input: String,
    val mainQuery: String,
    val friendlyResponse: String,
    val detailedQuery: String
)
