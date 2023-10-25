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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
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
    suspend fun Conversation.query(input: String, tables: List<String>, context: String?): QueriesAnswer
}

class QueryPrompterImpl(private val config: JdbcConfig) : QueryPrompter {
    private val logger = KotlinLogging.logger {}
    override suspend fun Conversation.promptQuery(
        prompt: String, tables: List<String>, context: String?
    ): QueryResult {
        logger.debug { "[Input]: $prompt" }
        Database.connect(url = config.toJDBCUrl(), user = config.username, password = config.password)
        val answer = query(prompt, tables, context)
        logger.debug { "[answer]: $answer" }
        return transaction {
            connection.prepareStatement(answer.mainQuery, false).executeQuery().toQueryResult()
        }
    }

    private fun getColumnsFromTables(tables: List<String>): String {
        val columns = tables.associateWith {
            transaction {
                val query = "select * from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='$it'"
                connection.prepareStatement(query, false).executeQuery().getColumnByName("column_name")
            }
        }
        logger.debug { "[Columns per table]: $columns" }
        return Json.encodeToString(columns)
    }

    override suspend fun Conversation.query(
        input: String,
        tables: List<String>,
        context: String?
    ): QueriesAnswer {
        val prompt = Prompt {
            +system(
                """
                 |You are an expert in SQL queries who has to generate the SQL queries.
                 |Select from this list of `tables` the SQL tables that you may need to solve the input.
                 |Keep into account today's date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                 |The queries have to be compatible with ${config.vendor}.
                 |Use the json `schema` to have more information about the fields of the table to answer properly.
                 |Use the `context` to accurate the answer.
                 |```tables
                 |$tables
                 |```
                 |```schema
                 |${getColumnsFromTables(tables)}
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
                 |Generate the expected output described in ExpectedOutput, the output has to accomplish the expectations of the user and the output format described above.
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
    val detailedQuery: String? = null
)