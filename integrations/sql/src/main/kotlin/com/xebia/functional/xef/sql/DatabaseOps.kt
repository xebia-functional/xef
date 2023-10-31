package com.xebia.functional.xef.sql

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.JDBCType

fun Database.tableDDL(tables: List<String>): String = transaction {
    tables.joinToString("\n") { tableDDL(it) }
}

fun Database.tableDDL(table: String): String = transaction {
    connection.metadata {
        val columns = this.columns(Table(table))
            .values
            .flatten()
            .joinToString(",\n", postfix = ",") {
                val dataType = JDBCType.valueOf(it.type).name
                val dataTypeWithLength = (if (it.size != null) "$dataType(${it.size})" else dataType).uppercase()
                val nullable = if (it.nullable) "NULL" else "NOT NULL"
                val defaultValue = if (it.defaultDbValue != null) "DEFAULT ${it.defaultDbValue}" else ""

                "\t${it.name} $dataTypeWithLength $nullable $defaultValue"
            }.replace(Regex("\\s*,"), ",")

        val primaryKeys = this.existingPrimaryKeys(Table(table))
            .values
            .fold("") { _, it -> it?.columnNames?.joinToString { a -> "\tPRIMARY KEY ($a),\n" } ?: "" }

        """TABLE $table (
          |$columns
          |$primaryKeys);
          |""".trimMargin()
    }
}
