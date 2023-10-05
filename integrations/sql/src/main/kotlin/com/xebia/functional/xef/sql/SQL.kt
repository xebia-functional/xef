package com.xebia.functional.xef.sql

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.sql.jdbc.JdbcConfig
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Properties
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface SQL {

  companion object {
    suspend fun <A> fromJdbcConfig(config: JdbcConfig, block: suspend SQL.() -> A) : A = JDBCSQLImpl(config).use {
      block(it)
    }

    fun fromJdbcConfigSync(config: JdbcConfig): SQL {
      return JDBCSQLImpl(config)
    }
  }

  /**
   * Generates SQL from the DDL and input prompt
   */
  @AiDsl
  suspend fun Conversation.sql(ddl: String, input: String): String

  /**
   * Chooses a subset of tables from the list of [tableNames] based on the [prompt]
   */
  @AiDsl
  suspend fun Conversation.selectTablesForPrompt(tableNames: String, prompt: String): String

  /**
   * Returns a list of documents found in the database for the given [prompt]
   */
  @AiDsl
  suspend fun Conversation.promptQuery(prompt: String): List<String>

  /**
   * Returns a recommendation of prompts that are interesting for the database
   * based on the internal ddl schema
   */
  @AiDsl
  suspend fun Conversation.getInterestingPromptsForDatabase(): String

}

private class JDBCSQLImpl(
  private val config: JdbcConfig
) : SQL, Connection by jdbcConnection(config) {

  val logger = KotlinLogging.logger {}

  override suspend fun Conversation.promptQuery(
    prompt: String,
  ): List<String> {
    val tableNames = getTableNames().joinToString("\n")
    val selection = selectTablesForPrompt(tableNames, prompt)
    logger.debug { "Selected tables: $selection" }
    val tables = selection.split(",").map {  it.trim() }
    val ddl = tables.joinToString("\n") { getTableDDL(it) }
    val sql = sql(ddl, prompt).trim()
    logger.debug { "SQL: $sql" }
    return documentsForQuery(prompt, sql)
  }

  override suspend fun Conversation.selectTablesForPrompt(
    tableNames: String, prompt: String
  ): String = config.model.promptMessage(
    Prompt("""|You are an AI assistant which selects the best tables from which the `goal` can be accomplished.
     |Select from this list of SQL `tables` the tables that you may need to solve the following `goal`
     |```tables
     |$tableNames
     |```
     |```goal
     |$prompt
     |```
     |Instructions:
     |1. Select the table that you think is the best to solve the `goal`.
     |2. The tables should be selected from the list of tables above.
     |3. The tables should be selected by their name.
     |4. Your response should include a list of tables separated by a comma.
     |Selection:""".trimMargin())
  )

  private suspend fun documentsForQuery(
    prompt: String,
    sql: String,
  ): List<String> = prepareStatement(sql).use { statement ->
    statement.executeQuery().use { resultSet ->
      val results = resultSet.toDocuments(prompt)
      logger.debug { "Found: ${results.size} records" }
      val splitter = TokenTextSplitter(
        encodingType = config.model.modelType.encodingType, chunkSize = config.model.maxContextLength / 2, chunkOverlap = 10
      )
      val splitDocuments = splitter.splitDocuments(results)
      logger.debug { "Split into: ${splitDocuments.size} documents" }
      splitDocuments
    }
  }

  override suspend fun Conversation.sql(ddl: String, input: String): String = config.model.promptMessage(
    Prompt("""|
       |You are an AI assistant which produces SQL SELECT queries in SQL format.
       |You only reply in valid SQL SELECT queries.
       |You don't produce any other type of responses.
       |Instructions:
       |
       |1. Produce a SELECT SQL query exclusively using this DDL:
       |```ddl
       |$ddl
       |```
       |2. The query should be a valid SELECT SQL query which produces relevant data for the following goal: 
       |```goal
       |$input
       |```
       |3. If the `goal` does not specify a limit the query must include a LIMIT 50 clause at the end.
       |4. The query should only select from the fields needed to solve the `goal`.
       |4. Under no circumstances the query should contain queries that perform updates, inserts, sets or deletes. Exclusively `select` statements should be provided as response.
       |5. IMPORTANT! No data destructive or mutating queries should be produced or someone may get hurt, it's not up to me, it's up to you.
       |6. The response should be a single line with no additional lines or characters and start with: SELECT...
       |7. Consider the user does not provide the `goal` in the same language as the `ddl` is expressed when generating the query.
       |```
    """.trimMargin())
  )

  override suspend fun Conversation.getInterestingPromptsForDatabase(): String = config.model.promptMessage(
    Prompt("""|You are an AI assistant which replies with a list of the best prompts based on the content of this database:
       |Instructions:
       |1. Select from this `ddl` 3 top prompts that the user could ask about this database
       |   in order to interact with it. 
       |```ddl
       |${getTableNames().joinToString("\n\n") { getTableDDL(it) }}
       |```
       |2. Do not include prompts about system, user and permissions related tables.
       |3. Return the list of recommended prompts separated by a comma.
       |""".trimMargin())
  )

  private fun getTableNames(): List<String> =
    metaData.getTables(null, null, "%", arrayOf("TABLE")).use { rs ->
      val tableNames = mutableListOf<String>()
      while (rs.next()) {
        tableNames.add(rs.getString("TABLE_NAME"))
      }
      tableNames
    }

  private fun getTableDDL(tableName: String): String =
    metaData.getColumns(null, null, tableName, null).use { rs ->
      buildString {
        append("CREATE TABLE $tableName (\n")
        while (rs.next()) {
          val columnName = rs.getString("COLUMN_NAME")
          val dataTypeName = rs.getString("TYPE_NAME")
          val columnSize = rs.getString("COLUMN_SIZE")
          append("  $columnName $dataTypeName($columnSize),\n")
        }
        deleteCharAt(length - 2) // Remove the last comma
        append(");")
      }
    }

  /**
   * Converts a JDBC ResultSet into a list of documents
   */
  private fun ResultSet.toDocuments(prompt: String): List<String> {
    val metaData = this.metaData
    val numColumns = metaData.columnCount
    val rows = mutableListOf<String>()
    while (this.next()) {
      val id = this.getString(1) // Get the ID from the first column
      if (id != null) {
        for (i in 2..numColumns) { // Start from the second column
          val value = this.getString(i)
          if (value != null) {
            val jsonEscaped = Json.encodeToString(value)
            val fieldName = metaData.getColumnName(i)
            val tableName = metaData.getTableName(i)
            // Each row is represented as a JSON object with ID, field name, and table name
            val row = "prompt: $prompt, tableName: $tableName, id: $id, $fieldName: $jsonEscaped\n"
            rows.add(row)
          }
        }
      }
    }
    return rows
  }

  companion object {
    private fun jdbcConnection(config: JdbcConfig): Connection {
      val connectionProps = Properties().apply {
        put("user", config.username)
        put("password", config.password)
      }
      return DriverManager.getConnection(config.toJDBCUrl(), connectionProps)
    }
  }
}
