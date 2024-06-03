package com.xebia.functional.xef.sql

import arrow.core.getOrElse
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.sql.ResultSetOps.toQueryResult
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface SQL {
    companion object {
        suspend fun <A> fromJdbcConfig(config: JdbcConfig, block: suspend SQL.() -> A): A = block(
            SQLImpl(
                config.chatApi,
                config.model,
                Database.connect(url = config.toJDBCUrl(), user = config.username, password = config.password)
            )
        )
    }

    /**
     * Generates a SQL query and obtains the table data from a user's prompt.
     *
     * @param prompt The input prompt.
     * @param tableNames A list of table names that may be needed for query generation.
     * @param context Additional context information.
     * @return An AnswerResponse object containing the response, query details, and result tables.
     */
    @AiDsl
    suspend fun Conversation.promptQuery(prompt: String, tableNames: List<String>, context: String?): AnswerResponse
}

class SQLImpl(private val chatApi: Chat, private val model: CreateChatCompletionRequestModel, private val db: Database) : SQL {
    private val logger = KotlinLogging.logger {}

    override suspend fun Conversation.promptQuery(
        prompt: String,
        tableNames: List<String>,
        context: String?
    ): AnswerResponse {
        val queriesAnswer = query(prompt, tableNames, context)
        val mainTable = queriesAnswer.mainQuery?.takeIf { it.isNotBlank() }?.let { executeSQL(it) }
        val detailedTable = queriesAnswer.detailedQuery?.takeIf { it.isNotBlank() }?.let { executeSQL(it) }
        val friendlyResponse = queriesAnswer.replaceFriendlyResponse(mainTable)

        logger.info { "Main query: ${queriesAnswer.mainQuery}" }
        logger.info { "Detailed query: ${queriesAnswer.detailedQuery}" }
        logger.info { "Friendly response: $friendlyResponse" }

        return AnswerResponse(
            input = prompt,
            answer = friendlyResponse,
            mainQuery = queriesAnswer.mainQuery,
            detailedQuery = queriesAnswer.detailedQuery,
            mainTable = mainTable,
            detailedTable = detailedTable
        )
    }

    private fun executeSQL(sql: String): QueryResult = transaction {
        runCatching { connection.prepareStatement(sql, false).executeQuery().toQueryResult() }.getOrElse {
            logger.info { "Failing executing SQL query: $sql" }
            QueryResult.empty()
        }
    }

    private suspend fun Conversation.query(
        input: String,
        tableNames: List<String>,
        context: String?
    ): QueriesAnswer {
        val prompt = Prompt(model) {
            +system(
                """
                 As an SQL expert, your main goal is to generate SQL queries, but you must be able to answer any SQL-related question that solves the input.

                 Keep into account today's date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                 The queries have to be compatible with ${db.vendor} in the version ${db.version}.
                 You must always use aliases to generate the queries.
                 
                 The database name is: ${db.name}
                 We have the tables named: ${tableNames.joinToString(", ")} whose SQL schema are the next:
                 ${tableDDL(tableNames)}
                 
                 "${context?.let { "Analyse the following context to accurate the answer: $it" }}"
                 
                 Please check the fields of the tables to be able to generate consistent and valid SQL results. 
                 Make sure that the result includes all the mandatory fields, and analyze if the optional ones are needed.

                 In case you have to generate a SQL query to solve the input:
                     - Select from this list of tables the SQL tables that you need to generate the query.
                     - Generate a main SQL query that has to satisfy the input of the user if it's possible.
                     - If the SQL query is successfully generated, provide a friendly sentence summary; otherwise, provide a friendly notice of failure.
                     - If the query generated returns an amount, the friendly sentence can refer the data with XXX and you have to generate a another query to show the disaggregated data.
                     - If the friendly sentence refers the data with XXX, add the column name to extract the value to replace it when I run the query.
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

        return chatApi.prompt(
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
    @Description("Friendly sentence that summarize the output.")
    val friendlyResponse: String,
    @Description("Column name to extract the data to replace the placeholder.")
    val columnToReplace: String?,
    @Description("SQL to show the disaggregated data.")
    val detailedQuery: String?
) {
    fun replaceFriendlyResponse(result: QueryResult?): String =
        if (friendlyResponse.contains("XXX")) {
            val value = columnToReplace?.let { colName ->
                result?.let { r -> r.rows.firstOrNull()?.elementAtOrNull(r.index(colName)) }
            } ?: ""
            friendlyResponse.replace("XXX", value)
        } else friendlyResponse
}

data class AnswerResponse(
    val input: String,
    val answer: String,
    val mainQuery: String?,
    val detailedQuery: String?,
    val mainTable: QueryResult?,
    val detailedTable: QueryResult?
)
