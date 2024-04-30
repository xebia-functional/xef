package com.xebia.functional.xef.server.assistants.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.AssistantObject
import com.xebia.functional.openai.generated.model.CreateAssistantRequest
import com.xebia.functional.openai.generated.model.ListAssistantsResponse
import com.xebia.functional.openai.generated.model.ModifyAssistantRequest
import com.xebia.functional.xef.server.db.tables.format
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object AssistantsTable : Table("xef_assistants") {
  val id = uuid("id")
  val data = jsonb<AssistantObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  fun create(createAssistantRequest: CreateAssistantRequest): AssistantObject = transaction {
    val uuid = UUID.randomUUID()
    val assistantObject = AssistantObject(
      id = uuid.toString(),
      `object` = AssistantObject.Object.assistant,
      createdAt = System.currentTimeMillis().toInt(),
      name = createAssistantRequest.name,
      description = createAssistantRequest.description,
      model = createAssistantRequest.model,
      instructions = createAssistantRequest.instructions,
      tools = createAssistantRequest.tools.orEmpty(),
      fileIds = createAssistantRequest.fileIds.orEmpty(),
      metadata = createAssistantRequest.metadata
    )
    AssistantsTable.insert {
      it[id] = uuid
      it[data] = assistantObject
    }
    assistantObject
  }

  fun get(assistantId: String): AssistantObject = transaction {
    AssistantsTable.select(
      data
    ).where {
      AssistantsTable.id eq UUID.fromString(assistantId)
    }.singleOrNull()?.let {
      it[data]
    } ?: throw Exception("Assistant not found for id: $assistantId")
  }

  fun delete(assistantId: String): Boolean = transaction {
    AssistantsTable.deleteWhere {
      AssistantsTable.id eq UUID.fromString(assistantId)
    } > 0
  }

  fun list(
    limit: Int?, order: Assistants.OrderListAssistants?, after: String?, before: String?
  ): ListAssistantsResponse {
    return transaction {
      val query = AssistantsTable.selectAll()
      val sortedQuery = when (order) {
        Assistants.OrderListAssistants.asc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
        Assistants.OrderListAssistants.desc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
        null -> query
      }
      val afterAssistant = after?.let { UUID.fromString(it) }
      val beforeAssistant = before?.let { UUID.fromString(it) }
      val afterAssistantIndex =
        afterAssistant?.let { sortedQuery.indexOfFirst { it[data].id == afterAssistant.toString() } }
      val beforeAssistantIndex =
        beforeAssistant?.let { sortedQuery.indexOfFirst { it[data].id == beforeAssistant.toString() } }
      val slicedQuery = when {
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

  fun modify(assistantId: String, modifyAssistantRequest: ModifyAssistantRequest): AssistantObject =
    transaction {
      val assistantObject = get(assistantId)
      val modifiedAssistantObject = assistantObject.copy(
        name = modifyAssistantRequest.name ?: assistantObject.name,
        description = modifyAssistantRequest.description ?: assistantObject.description,
        model = modifyAssistantRequest.model ?: assistantObject.model,
        instructions = modifyAssistantRequest.instructions ?: assistantObject.instructions,
        tools = modifyAssistantRequest.tools ?: assistantObject.tools,
        fileIds = modifyAssistantRequest.fileIds ?: assistantObject.fileIds,
        metadata = modifyAssistantRequest.metadata ?: assistantObject.metadata
      )
      AssistantsTable.update({ AssistantsTable.id eq UUID.fromString(assistantId) }) {
        it[data] = modifiedAssistantObject
      }
      modifiedAssistantObject
    }

}
