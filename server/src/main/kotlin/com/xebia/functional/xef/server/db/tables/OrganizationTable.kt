package com.xebia.functional.xef.server.db.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OrganizationTable : IntIdTable() {
    val name = varchar("name", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val ownerId = reference(
        name = "owner_id",
        refColumn = UsersTable.id,
        onDelete = ReferenceOption.CASCADE
    )
}

class Organization(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Organization>(OrganizationTable)

    var name by OrganizationTable.name
    var createdAt by OrganizationTable.createdAt
    var updatedAt by OrganizationTable.updatedAt
    var ownerId by OrganizationTable.ownerId
}

