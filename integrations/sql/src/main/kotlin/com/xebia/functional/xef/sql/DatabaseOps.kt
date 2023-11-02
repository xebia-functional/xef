package com.xebia.functional.xef.sql

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.JDBCType

fun tableDDL(tableNames: List<String>): String = transaction {
    tableNames.joinToString("\n") { tableName ->
        val table = Table(tableName)
        this.connection.metadata {
            val columns = this.columns(table)
                .values
                .flatten()
                .joinToString(",\n", postfix = ",") { column ->
                    val dataType = JDBCType.valueOf(column.type).name
                    val dataTypeWithLength =
                        (if (column.size != null) "$dataType(${column.size})" else dataType).uppercase()
                    val nullable = if (column.nullable) "NULL" else "NOT NULL"
                    val defaultValue = if (column.defaultDbValue != null) "DEFAULT ${column.defaultDbValue}" else ""

                    "\t${column.name} $dataTypeWithLength $nullable $defaultValue"
                }.replace(Regex("\\s*,"), ",")

            val primaryKeys = this.existingPrimaryKeys(table)
                .values
                .fold("") { _, it -> it?.columnNames?.joinToString { name -> "\tPRIMARY KEY ($name),\n" } ?: "" }

            """TABLE $tableName (
          |$columns
          |$primaryKeys);
          |""".trimMargin()
        }
    }
}
