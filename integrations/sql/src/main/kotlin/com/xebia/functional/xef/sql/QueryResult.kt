package com.xebia.functional.xef.sql

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val columns: List<Column>,
    val rows: List<Row>
) {
    companion object {
        fun empty() = QueryResult(emptyList(), emptyList())
    }

    fun getFieldByColumnName(name: String): String? =
        columns.indexOfFirst { it.name == name }.takeIf { it >= 0 }?.let { index ->
            rows.firstOrNull()?.findByIndex(index)
        }
}


@Serializable
data class Row(val fields: List<String?>) {
    fun findByIndex(index: Int): String? = fields.getOrNull(index)
}

@Serializable
data class Column(
    val name: String,
    val type: String
)
