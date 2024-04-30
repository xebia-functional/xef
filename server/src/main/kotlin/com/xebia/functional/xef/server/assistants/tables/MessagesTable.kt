package com.xebia.functional.xef.server.assistants.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.server.db.tables.format
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object MessagesTable : Table("xef_messages") {
  val id = uuid("id")
  val data = jsonb<MessageObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  fun list(threadId: String, limit: Int?,
           order: Assistants.OrderListMessages?,
           after: String?,
           before: String?): ListMessagesResponse = transaction {
    val query = MessagesTable.select(
      data
    ).where {
      data.extract<String>("threadId") eq threadId
    }
    val sortedQuery = when (order) {
      Assistants.OrderListMessages.asc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
      Assistants.OrderListMessages.desc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
      null -> query
    }
    val afterMessage = after?.let { UUID.fromString(it) }
    val beforeMessage = before?.let { UUID.fromString(it) }
    val afterMessageIndex = afterMessage?.let { sortedQuery.indexOfFirst { it[data].id == afterMessage.toString() } }
    val beforeMessageIndex = beforeMessage?.let { sortedQuery.indexOfFirst { it[data].id == beforeMessage.toString() } }
    val slicedQuery = when {
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

  fun get(threadId: String, messageId: String): MessageObject = transaction {
    MessagesTable.select(
      data
    ).where {
      (MessagesTable.id eq UUID.fromString(messageId)) and (data.extract<String>("threadId") eq threadId)
    }.singleOrNull()?.let {
      it[data]
    } ?: throw Exception("Message not found for id: $messageId")
  }

  fun create(
    threadId: String,
    assistantId: String?,
    runId: String?,
    createMessageRequest: CreateMessageRequest): MessageObject =
    transaction {
      val uuid = UUID.randomUUID()
      val threadObject = MessageObject(
        id = uuid.toString(),
        `object` = MessageObject.Object.thread_message,
        createdAt = System.currentTimeMillis().toInt(),
        threadId = threadId,
        role = MessageObject.Role.user,
        content = listOf(
          MessageObjectContentInner.CaseMessageContentTextObject(
            MessageContentTextObject(
              type = MessageContentTextObject.Type.text,
              text = MessageContentTextObjectText(
                value = createMessageRequest.content,
                annotations = emptyList()
              )
            )
          )
        ),
        assistantId = assistantId,
        runId = runId,
        fileIds = createMessageRequest.fileIds.orEmpty(),
        metadata = createMessageRequest.metadata
      )
      MessagesTable.insert {
        it[id] = uuid
        it[data] = threadObject
      }
      threadObject
    }

  fun modify(threadId: String, messageId: String, modifyMessageRequest: ModifyMessageRequest): MessageObject {
    return transaction {
      val messageObject = get(threadId, messageId)
      val modifiedMessageObject = messageObject.copy(
        metadata = modifyMessageRequest.metadata
      )
      MessagesTable.update({ MessagesTable.id eq UUID.fromString(messageId) }) {
        it[data] = modifiedMessageObject
      }
      modifiedMessageObject
    }
  }

}
