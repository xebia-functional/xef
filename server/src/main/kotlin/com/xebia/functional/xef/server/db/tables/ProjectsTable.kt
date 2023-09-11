package com.xebia.functional.xef.server.db.tables

import com.xebia.functional.xef.server.models.Projects
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ProjectsTable: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val orgId = reference(
        name = "org_id",
        refColumn = OrganizationTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toProject(): Projects {
    return Projects(
        id = this[ProjectsTable.id],
        name = this[ProjectsTable.name],
        createdAt = this[ProjectsTable.createdAt],
        updatedAt = this[ProjectsTable.updatedAt],
        orgId = this[ProjectsTable.orgId]
    )
}
