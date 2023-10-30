package com.xebia.functional.xef.server.db.psql.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UsersOrgsTable : Table("users_org") {
  val userId = reference(name = "user_id", foreign = UsersTable, onDelete = ReferenceOption.CASCADE)
  val orgId =
    reference(name = "org_id", foreign = OrganizationsTable, onDelete = ReferenceOption.CASCADE)

  override val primaryKey = PrimaryKey(userId, orgId)
}
