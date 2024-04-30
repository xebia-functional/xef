package com.xebia.functional.xef.server.assistants.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.server.db.tables.format
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object AssistantsFilesTable : Table("xef_assistants_files") {

  val id = uuid("id")
  val data = jsonb<AssistantFileObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  fun create(assistantId: String, createAssistantFileRequest: CreateAssistantFileRequest): AssistantFileObject =
    transaction {
      val uuid = UUID.randomUUID()
      val assistantFileObject = AssistantFileObject(
        id = createAssistantFileRequest.fileId,
        `object` = AssistantFileObject.Object.assistant_file,
        createdAt = System.currentTimeMillis().toInt(),
        assistantId = assistantId,
      )
      AssistantsFilesTable.insert {
        it[id] = uuid
        it[data] = assistantFileObject
      }
      assistantFileObject
    }

  fun delete(assistantId: String, fileId: String): Boolean =
    transaction {
      AssistantsFilesTable.deleteWhere {
        AssistantsFilesTable.data.extract<String>("id") eq fileId and
          (AssistantsFilesTable.data.extract<String>("assistantId") eq assistantId)
      } > 0
    }

  fun get(assistantId: String, fileId: String): AssistantFileObject =
    transaction {
      AssistantsFilesTable.select(
        data
      ).where {
        (AssistantsFilesTable.id eq UUID.fromString(fileId)) and
          (AssistantsFilesTable.data.extract<String>("assistantId") eq assistantId)
      }.singleOrNull()?.let {
        it[data]
      } ?: throw Exception("Assistant file not found for id: $fileId")
    }

  fun list(assistantId: String, limit: Int?, order: Assistants.OrderListAssistantFiles?, after: String?, before: String?): ListAssistantFilesResponse {
    val query = AssistantsFilesTable.select(
      data
    ).where {
      data.extract<String>("assistantId") eq assistantId
    }
    val sortedQuery = when (order) {
      Assistants.OrderListAssistantFiles.asc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
      Assistants.OrderListAssistantFiles.desc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
      null -> query
    }
    val afterFile = after?.let { UUID.fromString(it) }
    val beforeFile = before?.let { UUID.fromString(it) }
    val afterFileIndex = afterFile?.let { sortedQuery.indexOfFirst { it[data].id == afterFile.toString() } }
    val beforeFileIndex = beforeFile?.let { sortedQuery.indexOfFirst { it[data].id == beforeFile.toString() } }
    val slicedQuery = when {
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
