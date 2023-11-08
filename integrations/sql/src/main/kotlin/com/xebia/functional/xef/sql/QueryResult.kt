package com.xebia.functional.xef.sql

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val columns: List<Column>,
    val rows: List<List<String?>>
) {
    companion object {
        fun empty() = QueryResult(emptyList(), emptyList())
    }

    fun index(column: String): Int =
        columns.indexOfFirst { it.name == column }
}

@Serializable
data class Column(
    val name: String,
    val type: String
)
