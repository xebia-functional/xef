package com.xebia.functional.xef.sql

import kotlinx.serialization.Serializable
import java.sql.ResultSet

object ResultSetOps {
    @Serializable
    data class QueryResult(
        val columns: List<Column>,
        val rows: List<List<String?>>
    ) {
        val isSingleValue: Boolean = rows.size == 1 && columns.size == 1
    }

    @Serializable
    data class TableDDL(
        val table: String,
        val columns: List<Column>
    )

    @Serializable
    data class Column(
        val name: String,
        val type: String
    )

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

    fun ResultSet.toTableDDL(table: String): TableDDL {
        val rows = mutableListOf<Column>()

        while (next()) {
            val column = this.findColumn("column")
            val type = this.findColumn("type")
            rows.add(Column(getString(column), getString(type)))
        }

        return TableDDL(table, rows)
    }


}
