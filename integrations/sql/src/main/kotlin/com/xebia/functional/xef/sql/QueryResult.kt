package com.xebia.functional.xef.sql

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val columns: List<Column>,
    val rows: List<List<String?>>
)

@Serializable
data class QueryTableDDL(
    val table: String,
    val columns: List<Column>
)

@Serializable
data class Column(
    val name: String,
    val type: String
)
