package com.xebia.functional.xef.server.db.psql.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OrganizationsTable : IntIdTable("organizations") {
  val name = varchar("name", 20)
  val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
  val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
  val ownerId =
    reference(name = "owner_id", refColumn = UsersTable.id, onDelete = ReferenceOption.CASCADE)
}

class Organization(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Organization>(OrganizationsTable)

  var name by OrganizationsTable.name
  var createdAt by OrganizationsTable.createdAt
  var updatedAt by OrganizationsTable.updatedAt
  var ownerId by OrganizationsTable.ownerId

  var users by User via UsersOrgsTable
}
