package com.xebia.functional.xef.sql

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.sql.ResultSetOps.QueryResult
import com.xebia.functional.xef.sql.ResultSetOps.toQueryResult
import com.xebia.functional.xef.sql.ResultSetOps.toTableSchema
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface QueryPrompter {
    companion object {
        suspend fun <A> fromJdbcConfig(config: JdbcConfig, block: suspend QueryPrompter.() -> A): A = block(
            QueryPrompterImpl(
                config.model,
                Database.connect(url = config.toJDBCUrl(), user = config.username, password = config.password)
            )
        )

        suspend fun <A> fromDatabase(
            model: ChatWithFunctions,
            database: Database,
            block: suspend QueryPrompter.() -> A
        ): A = block(QueryPrompterImpl(model, database))
    }

    /**
     * Returns a queryResult found in the database for the given [prompt]
     */
    @AiDsl
    suspend fun Conversation.promptQuery(prompt: String, tables: List<String>, context: String?): AnswerResponse

    /**
     * Returns a recommendation of prompts that are interesting for the database
     * based on the internal ddl schema
     */
    @AiDsl
    suspend fun Conversation.getInterestingPromptsForDatabase(tables: List<String>): PromptsAnswer

    /**
     * Generates SQL queries based on table information and a context.
     */
    @AiDsl
    suspend fun Conversation.query(input: String, tables: List<String>, context: String?): QueriesAnswer
}

class QueryPrompterImpl(private val model: ChatWithFunctions, private val db: Database) : QueryPrompter {
    private val logger = KotlinLogging.logger {}

    override suspend fun Conversation.promptQuery(
        prompt: String,
        tables: List<String>,
        context: String?
    ): AnswerResponse {
        logger.debug { "[Input]: $prompt" }
        val queriesAnswer = query(prompt, tables, context)
        logger.debug { "[Queries]: $queriesAnswer" }
        val queryResult = queriesAnswer.mainQuery?.let { generateResult(it) }
        val answerReplaced = replaceFriendlyResponse(queriesAnswer, queryResult)
        return AnswerResponse(input = prompt, answer = answerReplaced, queryResult = queryResult)
    }

    private fun replaceFriendlyResponse(queriesAnswer: QueriesAnswer, result: QueryResult?): String =
        if (queriesAnswer.friendlyResponse.contains("XXX")) {
            val name = result?.columns?.find { it.name == queriesAnswer.columnToReplace }
            val columnIndex = result?.columns?.indexOf(name)
            val value = columnIndex?.let { if (it >= 0) result.rows[it].first() else "0" } ?: "0"
            queriesAnswer.friendlyResponse.replace("XXX", value)
        } else queriesAnswer.friendlyResponse


    private fun generateResult(sql: String): QueryResult = transaction(db) {
        connection.prepareStatement(sql, false).executeQuery().toQueryResult()
    }

    private fun getSchemaFromTables(tables: List<String>): String {
        val columns = tables.map { table ->
            transaction(db) {
                val schemaQuery = """
                    SELECT column_name, data_type FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='$table'
                """.trimIndent()
                connection.prepareStatement(schemaQuery, false).executeQuery().toTableSchema(table)
            }
        }
        return Json.encodeToString(columns)
    }

    override suspend fun Conversation.getInterestingPromptsForDatabase(tables: List<String>): PromptsAnswer {
        return model.prompt(
            Prompt(
                """
               |You are an AI assistant which replies with a list of the best prompts based on the content of this database:
               |Instructions:
               |1. Generate 3 prompts from this `ddl` 3 that the user could ask about this database
               |   in order to interact with it. 
               |```ddl
               |${getSchemaFromTables(tables)}}
               |```
               |2. Do not include prompts about system, user and permissions related tables.
               |3. Return the list of recommended prompts separated by a comma.
               |""".trimMargin()
            ), serializer<PromptsAnswer>()
        )
    }

    override suspend fun Conversation.query(input: String, tables: List<String>, context: String?): QueriesAnswer {
        val prompt = Prompt {
            +system(
                """
                 |You are an expert in SQL queries who has to generate the SQL query to solve the input.
                 |Keep into account today's date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                 |The queries have to be compatible with ${db.vendor} in the version ${db.version}.
                 |Use the json `schema` to have more information about the fields of the table to answer properly.
                 |Use the `context` to accurate the answer.
                 |```tables
                 |$tables
                 |```
                 |```schema
                 |${getSchemaFromTables(tables)}
                 |```
                 |```context
                 |$context
                 |```
                 |Instructions:
                 |1. Select from this list of `tables` the SQL tables that you may need to generate the query.
                 |2. Generate a main SQL query that has to satisfy the input of the user if it's possible.
                 |3. If it has been possible to generate the SQL query, create a friendly sentence summarizing the output; otherwise,
                 |   generate a friendly sentence indicating that it has not been possible. 
                 |4. You have to complement the main SQL query with another SQL query to provide more context.
                 |   If the main SQL query returns a single item or aggregated, You can generate a query to show the list of items involved.
                 |Queries must be valid to be executed.
                 |Only if the result of the SQL query is a single value, the friendly sentence can refer that data as XXX, that we can inject once we run the sql query. 
                 |Add the column name tu extract the value to replace after.
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

        return model.prompt(
            prompt = prompt,
            scope = this,
            serializer = serializer<QueriesAnswer>()
        )
    }

}

@Serializable
data class PromptsAnswer(val prompts: List<String>)

@Serializable
@Description("SQL queries")
data class QueriesAnswer(
    @Description("The SQL that satisfies the input of the user.")
    val mainQuery: String?,
    @Description("Column name to extract the data to replace the XXX")
    val columnToReplace: String?,
    @Description("Friendly sentence that summarize the output.")
    val friendlyResponse: String,
    @Description("SQL to complement the main query.")
    val detailedQuery: String?
)

data class AnswerResponse(
    val input: String,
    val answer: String,
    val queryResult: QueryResult? = null
)
