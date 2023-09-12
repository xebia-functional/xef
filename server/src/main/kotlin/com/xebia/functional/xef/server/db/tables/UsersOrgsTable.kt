package com.xebia.functional.xef.server.db.tables

import com.xebia.functional.xef.server.models.UsersOrgRelation
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object UsersOrgsTable : Table() {
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE
    )
    val orgId = reference(
        name = "org_id",
        foreign = OrganizationTable,
        onDelete = ReferenceOption.CASCADE
    )

    override val primaryKey = PrimaryKey(userId, orgId)
}

fun ResultRow.toUsersOrgs(): UsersOrgRelation {
    return UsersOrgRelation(
        userId = this[UsersOrgsTable.userId].value,
        orgId = this[UsersOrgsTable.orgId].value
    )
}
