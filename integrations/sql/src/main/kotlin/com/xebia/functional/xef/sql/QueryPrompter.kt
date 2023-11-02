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
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
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
    }

    /**
     * Returns a queryResult found in the database for the given [prompt]
     */
    @AiDsl
    suspend fun Conversation.promptQuery(prompt: String, tableNames: List<String>, context: String?): AnswerResponse

    /**
     * Generates SQL queries based on table information and a context.
     */
    @AiDsl
    suspend fun Conversation.query(input: String, tableNames: List<String>, context: String?): QueriesAnswer
}

class QueryPrompterImpl(private val model: ChatWithFunctions, private val db: Database) : QueryPrompter {
    private val logger = KotlinLogging.logger {}


    override suspend fun Conversation.promptQuery(
        prompt: String,
        tableNames: List<String>,
        context: String?
    ): AnswerResponse {
        val queriesAnswer = query(prompt, tableNames, context)
        logger.debug { "[INPUT]: $prompt" }
        logger.debug { "[MAIN QUERY]: ${queriesAnswer.mainQuery}" }
        logger.debug { "[DETAILED QUERY]: ${queriesAnswer.detailedQuery}" }
        logger.debug { "[COLUMN]: ${queriesAnswer.columnToReplace}" }
        val mainTable = queriesAnswer.mainQuery?.let { executeSQL(it) }
        val detailedTable =
            queriesAnswer.detailedQuery?.let { if (it.isNotBlank()) executeSQL(it) else null }
        val friendlyResponseReplaced = replaceFriendlyResponse(queriesAnswer, mainTable)
        logger.debug { "[FRIENDLY RESPONSE]: $friendlyResponseReplaced" }
        return AnswerResponse(
            input = prompt,
            answer = friendlyResponseReplaced,
            mainQuery = queriesAnswer.mainQuery,
            detailedQuery = queriesAnswer.detailedQuery,
            mainTable = mainTable,
            detailedTable = detailedTable
        )
    }

    private fun replaceFriendlyResponse(queriesAnswer: QueriesAnswer, result: QueryResult?): String =
        if (queriesAnswer.friendlyResponse.contains("XXX")) {
            val columnIndex = result?.columns?.indexOfFirst { it.name == queriesAnswer.columnToReplace }
            logger.debug { "[COLUMN INDEX]: $columnIndex" }
            val value = columnIndex?.let { if (it >= 0) result.rows[it].first() else "" } ?: ""
            queriesAnswer.friendlyResponse.replace("XXX", value)
        } else queriesAnswer.friendlyResponse

    private fun executeSQL(sql: String): QueryResult = transaction {
        connection.prepareStatement(sql, false).executeQuery().toQueryResult()
    }

    override suspend fun Conversation.query(
        input: String,
        tableNames: List<String>,
        context: String?
    ): QueriesAnswer {
        val prompt = Prompt {
            +system(
                """
                 As an SQL expert, your main goal is to generate SQL queries, but you must be able to answer any SQL-related question that solves the input.
                 
                 Keep into account today's date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                 The queries have to be compatible with ${db.vendor} in the version ${db.version}.
                 Aggregate data must have an alias.
                 
                 We have the tables named: ${tableNames.joinToString { "," }} whose SQL schema are the next:
                 ${tableDDL(tableNames)}
                 Please check the fields of the tables to be able to generate consistent and valid SQL results. 

                 "${context.let { "Analyse the following context to accurate the answer: $it" }}"
                                  
                 In case you have to generate a SQL query to solve the input:
                     - Select from this list of `tables` the SQL tables that you may need to generate the query.
                     - Generate a main SQL query that has to satisfy the input of the user if it's possible.
                     - If the SQL query is successfully generated, provide a friendly sentence summary; otherwise, provide a friendly notice of failure.
                     - If the query generated returns a single item, the friendly sentence can refer the data with XXX.
                     - If the friendly sentence refer the data with XXX, add the column name tu extract the value to replace it when I run the query otherwise is null.
                     - If the main SQL query returns a single item, analyze if it is useful to generate another SQL query to show the data involved.
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
@Description("SQL queries")
data class QueriesAnswer(
    @Description("The main SQL that satisfies the input of the user.")
    val mainQuery: String?,
    @Description("Friendly sentence that summarize the input.")
    val friendlyResponse: String,
    @Description("Column name to extract the data to replace the placeholder.")
    val columnToReplace: String?,
    @Description("The optional SQL to show the data involved in the main query.")
    val detailedQuery: String?
)

data class AnswerResponse(
    val input: String,
    val answer: String,
    val mainQuery: String?,
    val detailedQuery: String?,
    val mainTable: QueryResult?,
    val detailedTable: QueryResult?
)
