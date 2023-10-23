package com.xebia.functional.xef.sql

import java.sql.ResultSet

object ResultSetOps {
    /**
     * Converts a JDBC ResultSet into a QueryResult
     */
    fun ResultSet.toQueryResult(): QueryResult {
        val columns = mutableListOf<Column>()
        val rows = mutableListOf<List<String?>>()

        for (i in 1..metaData.columnCount) {
            val fieldName = metaData.getColumnName(i)
            val fieldType = metaData.getColumnTypeName(i)
            columns.add(Column(fieldName, fieldType))
        }

        while (next()) {
            val row = mutableListOf<String?>()
            for (i in 1..metaData.columnCount) row.add(getString(i))
            rows.add(row)
        }

        return QueryResult(columns, rows)
    }

    fun ResultSet.getColumnByName(name: String): List<String> {
        val rows = mutableListOf<String>()
        while (next()) {
            rows.add(getString(name))
        }
        return rows
    }
}