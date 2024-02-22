package com.xebia.functional.xef.store

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphResponse(
  val columns: List<String>,
  val result: JsonElement
)


// Models access to a graph via Cypher queries
interface GraphStore {

  /**
   * Executes a Cypher query on the graph and returns the result.
   *
   * @param query The Cypher query to execute.
   * @return The result of the query execution.
   */
  fun executeQuery(query: String): GraphResponse

}
