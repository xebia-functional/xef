package com.xebia.functional.xef.server.db.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ProjectsTable: IntIdTable() {
    val name = varchar("name", 20)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
    val orgId = reference(
        name = "org_id",
        refColumn = OrganizationTable.id,
        onDelete = ReferenceOption.CASCADE
    )
}

class Project(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Project>(ProjectsTable)

    var name by ProjectsTable.name
    var createdAt by ProjectsTable.createdAt
    var updatedAt by ProjectsTable.updatedAt
    var orgId by ProjectsTable.orgId
}

