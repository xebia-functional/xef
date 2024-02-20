package com.xebia.functional.xef.store

import kotlinx.serialization.KSerializer
import javax.sql.DataSource

import kotlinx.serialization.json.Json
import java.sql.Connection

class PGGraphStore(
  private val dataSource: DataSource,
) : GraphStore {

  private fun setupAge(connection: Connection): Unit = connection.createStatement().use { statement ->
    statement.execute("LOAD 'age';")
    statement.execute("SET search_path = ag_catalog, \"\$user\", public;")
  }

  fun initialDbSetup(): Unit =
    setupAge(dataSource.connection)


  override fun <A> executeQuery(query: String): GraphResponse<A> {
    dataSource.connection.use { conn ->
      // Setup Apache AGE for the session
      setupAge(conn)

      // Execute the Cypher query
      val resultSet = conn.createStatement().use { stmt ->
        stmt.executeQuery("SELECT cypher('ag_catalog', \$\$ $query \$\$) as result;")
      }

      // Assuming the result is a JSON string for simplicity
      if (resultSet.next()) {
        val resultJson = resultSet.getString("result")
        println(resultJson)
        return TODO()
      } else {
        throw IllegalArgumentException("No result for query: $query")
      }
    }
  }
}

