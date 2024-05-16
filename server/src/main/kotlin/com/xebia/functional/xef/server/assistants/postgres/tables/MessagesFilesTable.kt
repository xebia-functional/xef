package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.ListMessageFilesResponse
import com.xebia.functional.openai.generated.model.MessageFileObject
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.server.db.tables.format
import java.util.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction

object MessagesFilesTable : Table("xef_messages_files"), AssistantPersistence.MessageFile {

  val id = uuid("id")
  val threadId = uuid("thread_id")
  val data = jsonb<MessageFileObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun get(threadId: String, messageId: String, fileId: String): MessageFileObject {
    return transaction {
      MessagesFilesTable.select(data)
        .where {
          (data.extract<String>("id") eq fileId) and
            (MessagesFilesTable.threadId eq UUID.fromString(threadId)) and
            (data.extract<String>("messageId") eq messageId)
        }
        .singleOrNull()
        ?.let { it[data] } ?: throw Exception("Message file not found for id: $fileId")
    }
  }

  override suspend fun list(
    threadId: String,
    messageId: String,
    limit: Int?,
    order: Assistants.OrderListMessageFiles?,
    after: String?,
    before: String?
  ): ListMessageFilesResponse {
    return transaction {
      val query =
        MessagesFilesTable.select(data).where {
          (MessagesFilesTable.threadId eq UUID.fromString(threadId)) and
            (data.extract<String>("messageId") eq messageId)
        }
      val sortedQuery =
        when (order) {
          Assistants.OrderListMessageFiles.asc ->
            query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
          Assistants.OrderListMessageFiles.desc ->
            query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
          null -> query
        }
      val afterMessage = after?.let { UUID.fromString(it) }
      val beforeMessage = before?.let { UUID.fromString(it) }
      val afterMessageIndex =
        afterMessage?.let { sortedQuery.indexOfFirst { it[data].id == afterMessage.toString() } }
      val beforeMessageIndex =
        beforeMessage?.let { sortedQuery.indexOfFirst { it[data].id == beforeMessage.toString() } }
      val slicedQuery =
        when {
          afterMessageIndex != null -> sortedQuery.drop(afterMessageIndex + 1)
          beforeMessageIndex != null -> sortedQuery.take(beforeMessageIndex)
          else -> sortedQuery
        }
      val limitedQuery = limit?.let { slicedQuery.take(it) } ?: slicedQuery
      ListMessageFilesResponse(
        `object` = "list",
        data = limitedQuery.map { it[data] },
        firstId = limitedQuery.firstOrNull()?.get(data)?.id,
        lastId = limitedQuery.lastOrNull()?.get(data)?.id,
        hasMore = sortedQuery.count() > limitedQuery.count()
      )
    }
  }
}
