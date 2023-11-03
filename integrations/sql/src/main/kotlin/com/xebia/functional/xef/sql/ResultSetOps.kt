package com.xebia.functional.xef.sql

import java.sql.ResultSet

object ResultSetOps {

    fun ResultSet.toQueryResult(): QueryResult {
        val columns = this.getColumns()
        val rows = mutableListOf<List<String?>>()

        while (this.next()) {
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
}
