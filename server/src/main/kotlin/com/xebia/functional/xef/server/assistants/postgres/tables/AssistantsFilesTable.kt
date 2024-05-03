package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.assistantFileObject
import com.xebia.functional.xef.server.db.tables.format
import java.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction

object AssistantsFilesTable : Table("xef_assistants_files"), AssistantPersistence.AssistantFiles {

  val id = uuid("id")
  val data = jsonb<AssistantFileObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun create(
    assistantId: String,
    createAssistantFileRequest: CreateAssistantFileRequest
  ): AssistantFileObject = transaction {
    val uuid = UUID.randomUUID()
    val assistantFileObject = assistantFileObject(createAssistantFileRequest, assistantId)
    AssistantsFilesTable.insert {
      it[id] = uuid
      it[data] = assistantFileObject
    }
    assistantFileObject
  }

  override suspend fun delete(assistantId: String, fileId: String): Boolean = transaction {
    AssistantsFilesTable.deleteWhere {
      data.extract<String>("id") eq fileId and (data.extract<String>("assistantId") eq assistantId)
    } > 0
  }

  override suspend fun get(assistantId: String, fileId: String): AssistantFileObject = transaction {
    AssistantsFilesTable.select(data)
      .where {
        (AssistantsFilesTable.id eq UUID.fromString(fileId)) and
          (data.extract<String>("assistantId") eq assistantId)
      }
      .singleOrNull()
      ?.let { it[data] } ?: throw Exception("Assistant file not found for id: $fileId")
  }

  override suspend fun list(
    assistantId: String,
    limit: Int?,
    order: Assistants.OrderListAssistantFiles?,
    after: String?,
    before: String?
  ): ListAssistantFilesResponse {
    val query =
      AssistantsFilesTable.select(data).where { data.extract<String>("assistantId") eq assistantId }
    val sortedQuery =
      when (order) {
        Assistants.OrderListAssistantFiles.asc ->
          query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
        Assistants.OrderListAssistantFiles.desc ->
          query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
        null -> query
      }
    val afterFile = after?.let { UUID.fromString(it) }
    val beforeFile = before?.let { UUID.fromString(it) }
    val afterFileIndex =
      afterFile?.let { sortedQuery.indexOfFirst { it[data].id == afterFile.toString() } }
    val beforeFileIndex =
      beforeFile?.let { sortedQuery.indexOfFirst { it[data].id == beforeFile.toString() } }
    val slicedQuery =
      when {
        afterFileIndex != null -> sortedQuery.drop(afterFileIndex + 1)
        beforeFileIndex != null -> sortedQuery.take(beforeFileIndex)
        else -> sortedQuery
      }
    val limitedQuery = limit?.let { slicedQuery.take(it) } ?: slicedQuery
    return ListAssistantFilesResponse(
      `object` = "list",
      data = limitedQuery.map { it[data] },
      firstId = limitedQuery.firstOrNull()?.get(data)?.id,
      lastId = limitedQuery.lastOrNull()?.get(data)?.id,
      hasMore = sortedQuery.count() > limitedQuery.count()
    )
  }
}
