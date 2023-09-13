package com.xebia.functional.xef.server.db.tables

import com.xebia.functional.xef.server.models.ProvidersConfig
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

val format = Json { prettyPrint = true }

data class XefTokens(
    @SerialName("user_id") val userId: Int,
    @SerialName("project_id") val projectId: Int,
    @SerialName("name") val name: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("token") val token: String,
    @SerialName("providers_config") val providersConfig: ProvidersConfig
)

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
