package com.xebia.functional.xef.server.db.psql.tables

import com.xebia.functional.xef.server.models.ProvidersConfig
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

val format = Json { prettyPrint = true }

object XefTokensTable : IntIdTable("xef_tokens") {
  val userId = reference(name = "user_id", foreign = UsersTable, onDelete = ReferenceOption.CASCADE)
  val projectId =
    reference(name = "project_id", foreign = ProjectsTable, onDelete = ReferenceOption.CASCADE)
  val name = varchar("name", 20)
  val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
  val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
  val token = varchar("token", 128).uniqueIndex()
  val providersConfig = jsonb<ProvidersConfig>("providers_config", format)
}

class XefTokens(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<XefTokens>(XefTokensTable)

  var userId by XefTokensTable.userId
  var projectId by XefTokensTable.projectId
  var name by XefTokensTable.name
  var createdAt by XefTokensTable.createdAt
  var updatedAt by XefTokensTable.updatedAt
  var token by XefTokensTable.token
  var providersConfig by XefTokensTable.providersConfig
}
