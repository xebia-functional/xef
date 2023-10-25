package com.xebia.functional.xef.sql

import com.xebia.functional.xef.sql.ResultSetOps.getColumns
import java.sql.ResultSet

object ResultSetOps {
    /**
     * Converts a JDBC ResultSet into a QueryResult.
     */
    fun ResultSet.toQueryResult(): QueryResult {
        val columns = this.getColumns()
        val rows = mutableListOf<List<String?>>()

        while (next()) {
            val row = mutableListOf<String?>()
            for (i in 1..this.metaData.columnCount) row.add(getString(i))
            rows.add(row)
        }

        return QueryResult(columns, rows)
    }

    private fun ResultSet.getColumns(): List<Column> {
        val columns = mutableListOf<Column>()

        for (i in 1..this.metaData.columnCount) {
            val fieldName = this.metaData.getColumnName(i)
            val fieldType = this.metaData.getColumnTypeName(i)
            columns.add(Column(fieldName, fieldType))
        }

        return columns
    }

    /**
     * Obtains the values of a column of a JDBC ResultSet.
     */
    fun ResultSet.getColumnByName(name: String): List<String> {
        val rows = mutableListOf<String>()
        while (next()) {
            rows.add(this.getString(name))
        }
        return rows
    }
}