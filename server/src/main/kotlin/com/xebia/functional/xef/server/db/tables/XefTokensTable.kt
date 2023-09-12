package com.xebia.functional.xef.server.db.tables

import com.xebia.functional.xef.server.db.tables.OrganizationTable.defaultExpression
import com.xebia.functional.xef.server.models.ProvidersConfig
import com.xebia.functional.xef.server.models.XefTokens
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

val format = Json { prettyPrint = true }

object XefTokensTable : Table() {
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE)
    val projectId = reference(
        name = "project_id",
        foreign = ProjectsTable,
        onDelete = ReferenceOption.CASCADE
    )
    val name = varchar("name", 20)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
    val token = varchar("token", 128).uniqueIndex()
    val providersConfig = jsonb<ProvidersConfig>("providers_config", format)

    override val primaryKey = PrimaryKey(userId, projectId, name)
}

fun ResultRow.toXefTokens() : XefTokens {
    return XefTokens(
        userId = this[XefTokensTable.userId].value,
        projectId = this[XefTokensTable.projectId].value,
        name = this[XefTokensTable.name],
        createdAt = this[XefTokensTable.createdAt],
        updatedAt = this[XefTokensTable.updatedAt],
        token = this[XefTokensTable.token],
        providersConfig = this[XefTokensTable.providersConfig]
    )
}
