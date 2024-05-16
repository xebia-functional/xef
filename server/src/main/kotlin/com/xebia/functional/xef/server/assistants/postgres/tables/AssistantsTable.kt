package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.AssistantObject
import com.xebia.functional.openai.generated.model.CreateAssistantRequest
import com.xebia.functional.openai.generated.model.ListAssistantsResponse
import com.xebia.functional.openai.generated.model.ModifyAssistantRequest
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.assistantObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.modifiedAssistantObject
import com.xebia.functional.xef.server.db.tables.format
import java.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction

object AssistantsTable : Table("xef_assistants"), AssistantPersistence.Assistant {
  val id = uuid("id")
  val data = jsonb<AssistantObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun create(createAssistantRequest: CreateAssistantRequest): AssistantObject =
    transaction {
      val uuid = UUID.randomUUID()
      val assistantObject =
        assistantObject(kotlinx.uuid.UUID(uuid.toString()), createAssistantRequest)
      AssistantsTable.insert {
        it[id] = uuid
        it[data] = assistantObject
      }
      assistantObject
    }

  override suspend fun get(assistantId: String): AssistantObject = transaction {
    AssistantsTable.select(data)
      .where { AssistantsTable.id eq UUID.fromString(assistantId) }
      .singleOrNull()
      ?.let { it[data] } ?: throw Exception("Assistant not found for id: $assistantId")
  }

  override suspend fun delete(assistantId: String): Boolean = transaction {
    AssistantsTable.deleteWhere { id eq UUID.fromString(assistantId) } > 0
  }

  override suspend fun list(
    limit: Int?,
    order: Assistants.OrderListAssistants?,
    after: String?,
    before: String?
  ): ListAssistantsResponse {
    return transaction {
      val query = AssistantsTable.selectAll()
      val sortedQuery =
        when (order) {
          Assistants.OrderListAssistants.asc ->
            query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
          Assistants.OrderListAssistants.desc ->
            query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
          null -> query
        }
      val afterAssistant = after?.let { UUID.fromString(it) }
      val beforeAssistant = before?.let { UUID.fromString(it) }
      val afterAssistantIndex =
        afterAssistant?.let {
          sortedQuery.indexOfFirst { it[data].id == afterAssistant.toString() }
        }
      val beforeAssistantIndex =
        beforeAssistant?.let {
          sortedQuery.indexOfFirst { it[data].id == beforeAssistant.toString() }
        }
      val slicedQuery =
        when {
          afterAssistantIndex != null -> sortedQuery.drop(afterAssistantIndex + 1)
          beforeAssistantIndex != null -> sortedQuery.take(beforeAssistantIndex)
          else -> sortedQuery
        }
      val limitedQuery = limit?.let { slicedQuery.take(it) } ?: slicedQuery
      ListAssistantsResponse(
        `object` = "list",
        data = limitedQuery.map { it[data] },
        firstId = limitedQuery.firstOrNull()?.get(data)?.id,
        lastId = limitedQuery.lastOrNull()?.get(data)?.id,
        hasMore = sortedQuery.count() > limitedQuery.count()
      )
    }
  }

  override suspend fun modify(
    assistantId: String,
    modifyAssistantRequest: ModifyAssistantRequest
  ): AssistantObject {
    val assistantObject = get(assistantId)
    return transaction {
      val modifiedAssistantObject = modifiedAssistantObject(assistantObject, modifyAssistantRequest)
      AssistantsTable.update({ AssistantsTable.id eq UUID.fromString(assistantId) }) {
        it[data] = modifiedAssistantObject
      }
      modifiedAssistantObject
    }
  }
}
