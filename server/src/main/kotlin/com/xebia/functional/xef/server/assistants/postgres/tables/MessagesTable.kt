package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.createMessageObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.modifiedMessageObject
import com.xebia.functional.xef.server.db.tables.format
import java.util.*
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction

object MessagesTable : Table("xef_messages"), AssistantPersistence.Message {
  val id = uuid("id")
  val data = jsonb<MessageObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun list(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListMessages?,
    after: String?,
    before: String?
  ): ListMessagesResponse = transaction {
    val query = MessagesTable.select(data).where { data.extract<String>("threadId") eq threadId }
    val sortedQuery =
      when (order) {
        Assistants.OrderListMessages.asc ->
          query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
        Assistants.OrderListMessages.desc ->
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
    ListMessagesResponse(
      `object` = "list",
      data = limitedQuery.map { it[data] },
      firstId = limitedQuery.firstOrNull()?.get(data)?.id,
      lastId = limitedQuery.lastOrNull()?.get(data)?.id,
      hasMore = sortedQuery.count() > limitedQuery.count()
    )
  }

  override suspend fun get(threadId: String, messageId: String): MessageObject = transaction {
    MessagesTable.select(data)
      .where {
        (MessagesTable.id eq UUID.fromString(messageId)) and
          (data.extract<String>("threadId") eq threadId)
      }
      .singleOrNull()
      ?.let { it[data] } ?: throw Exception("Message not found for id: $messageId")
  }

  override suspend fun createMessage(
    threadId: String,
    assistantId: String,
    runId: String,
    content: String,
    fileIds: List<String>,
    metadata: JsonObject?,
    role: MessageObject.Role
  ): MessageObject = transaction {
    val uuid = UUID.randomUUID()
    val msg =
      createMessageObject(
        kotlinx.uuid.UUID(uuid.toString()),
        threadId,
        role,
        content,
        assistantId,
        runId,
        fileIds,
        metadata
      )
    MessagesTable.insert {
      it[id] = uuid
      it[data] = msg
    }
    msg
  }

  override suspend fun modify(
    threadId: String,
    messageId: String,
    modifyMessageRequest: ModifyMessageRequest
  ): MessageObject {
    val messageObject = get(threadId, messageId)
    return transaction {
      val modifiedMessageObject = messageObject.copy(metadata = modifyMessageRequest.metadata)
      MessagesTable.update({ MessagesTable.id eq UUID.fromString(messageId) }) {
        it[data] = modifiedMessageObject
      }
      modifiedMessageObject
    }
  }

  override suspend fun updateContent(
    threadId: String,
    messageId: String,
    content: String
  ): MessageObject {
    val messageObject = get(threadId, messageId)
    return transaction {
      val modifiedMessageObject = modifiedMessageObject(messageObject, content)
      MessagesTable.update({ MessagesTable.id eq UUID.fromString(id) }) {
        it[data] = modifiedMessageObject
      }
      modifiedMessageObject
    }
  }
}
