package com.xebia.functional.xef.server.db.tables

import com.xebia.functional.xef.server.models.Organization
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OrganizationTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val ownerId = reference(
        name = "owner_id",
        refColumn = UsersTable.id,
        onDelete = ReferenceOption.CASCADE
    )

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toOrganization(): Organization {
    return Organization(
        id = this[OrganizationTable.id],
        name = this[OrganizationTable.name],
        createdAt = this[OrganizationTable.createdAt],
        updatedAt = this[OrganizationTable.updatedAt],
        ownerId = this[OrganizationTable.ownerId]
    )
}

