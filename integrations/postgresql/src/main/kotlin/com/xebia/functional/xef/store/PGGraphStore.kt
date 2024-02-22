package com.xebia.functional.xef.store

import com.xebia.functional.xef.store.config.PostgreSQLGraphStoreConfig
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.neo4j.cypher.internal.frontend.v3_4.parser.`CypherParser$`
import scala.Option
import scala.collection.JavaConversions
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

class PGGraphStore(
  private val graphId: String,
  private val config: PostgreSQLGraphStoreConfig,
  private val dataSource: DataSource,
  private val logger: Logger,
) : GraphStore {

  private fun setupAge(connection: Connection): Unit = connection.createStatement().use { statement ->
    statement.execute("LOAD 'age';")
    statement.execute("SET search_path = ag_catalog, \"postgres\", public;")
  }

  fun init(): Unit {
    dataSource.connection.use { conn ->
      conn.createStatement().use { stmt ->

        //check if the graph exists
        stmt.executeQuery("SELECT 1 FROM ag_catalog.ag_graph WHERE name = '$graphId'").use { resultSet ->

          if (!resultSet.next()) {
            // Create the graph if it does not exist
            val result = stmt.execute("SELECT create_graph('$graphId');")
            if (!result) {
              throw IllegalStateException("Failed to create graph $graphId")
            }
          } else {
            val result = resultSet.getObject(1)
            logger.info("Result: $result")
          }
        }
      }
    }
  }


  override fun executeQuery(query: String): GraphResponse =
    dataSource.connection.use { conn ->
      // Setup Apache AGE for the session
      setupAge(conn)
      val parser = `CypherParser$`.`MODULE$`
      val parsedQuery = parser.parseOrThrow(query, Option.empty(), parser.Statements())
      val returnedColumns: List<String> = JavaConversions.asJavaIterable(parsedQuery.returnColumns()).toList()
      val returnQuery =
        if (returnedColumns.isEmpty()) {
          "(result0 agtype)"
        } else {
          returnedColumns.joinToString(prefix = "(", postfix = ")", separator = ",") { "$it agtype" }
        }

      // Execute the Cypher query
      conn.createStatement().use { stmt ->
        stmt.executeQuery("SELECT * FROM cypher('$graphId', \$\$ $query \$\$) as $returnQuery;").use { rs ->
          val columnCount = rs.metaData.columnCount
          val labels = (1..columnCount).map { rs.metaData.getColumnLabel(it) }
          // Assuming the result is a JSON string for simplicity
          val list = mutableListOf<JsonElement>()
          while (rs.next()) {
            parseGraphResponse(returnedColumns, rs, query).let(list::add)
          }
          val result = if (list.size == 1) {
            list.first()
          } else {
            JsonArray(list)
          }
          GraphResponse(labels, result)
        }
      }
    }

  private fun parseGraphResponse(
    returnedColumns: List<String>,
    rs: ResultSet,
    query: String
  ): JsonElement {
    val list = mutableListOf<JsonElement>()
    for (alias in returnedColumns) {
      val resultJson = rs.getString(alias) ?: throw IllegalStateException("No result for query: $query")
      graphResponse(resultJson)?.let(list::add)
    }
    return if (list.size == 1) {
      list.first()
    } else {
      JsonArray(list)
    }
  }


  private fun graphResponse(
    resultJson: String
  ): JsonElement? {
    val json = resultJson.substringBeforeLast("::")
    val type = resultJson.substringAfterLast("::")
    return when (type) {
      "vertex" -> parseNode(json)
      "edge" -> parseRelationship(json)
      "path" -> parsePath(json)
      else -> try {
        Json.parseToJsonElement(json)
      } catch (e: Exception) {
        logger.error("Failed to parse JSON: $json", e)
        null
      }
    }
  }

  private fun parsePath(json: String): JsonArray {
    // Regex pattern to match JSON objects followed by ::vertex or ::edge
    val pattern = """\{.*?}::(vertex|edge)""".toRegex(RegexOption.DOT_MATCHES_ALL)

    // Find all matches in the input string
    val matches = pattern.findAll(json)

    // Extracting the matched strings
    val elements = matches.map { it.value }.toList()
    val path = JsonArray(elements.mapNotNull { graphResponse(it) })
    return path
  }

  private fun parseRelationship(json: String): JsonObject =
    Json.decodeFromString(JsonObject.serializer(), json)

  private fun parseNode(json: String): JsonObject =
    Json.decodeFromString(JsonObject.serializer(), json)


}

