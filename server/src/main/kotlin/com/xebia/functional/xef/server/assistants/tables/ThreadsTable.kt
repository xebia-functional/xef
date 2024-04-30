package com.xebia.functional.xef.server.assistants.tables

import com.xebia.functional.openai.generated.model.CreateThreadRequest
import com.xebia.functional.openai.generated.model.ModifyThreadRequest
import com.xebia.functional.openai.generated.model.ThreadObject
import com.xebia.functional.xef.server.db.tables.format
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object ThreadsTable : Table("xef_threads") {
  val id = uuid("id")
  val data = jsonb<ThreadObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  fun create(
    assistantId: String?,
    runId: String?,
    createThreadRequest: CreateThreadRequest
  ): ThreadObject =
    transaction {
      val uuid = UUID.randomUUID()
      val threadObject = ThreadObject(
        id = uuid.toString(),
        `object` = ThreadObject.Object.thread,
        createdAt = System.currentTimeMillis().toInt(),
        metadata = createThreadRequest.metadata
      )
      ThreadsTable.insert {
        it[id] = uuid
        it[data] = threadObject
      }
      createThreadRequest.messages.orEmpty().forEach {
        MessagesTable.create(
          threadId = threadObject.id,
          assistantId = assistantId,
          runId = runId,
          createMessageRequest = it
        )
      }
      threadObject
    }

  fun get(threadId: String): ThreadObject {
    return transaction {
      ThreadsTable.select(
        data
      ).where {
        ThreadsTable.id eq UUID.fromString(threadId)
      }.singleOrNull()?.let {
        it[data]
      } ?: throw Exception("Thread not found for id: $threadId")
    }
  }

  fun delete(threadId: String): Boolean =
    transaction {
      ThreadsTable.deleteWhere {
        ThreadsTable.id eq UUID.fromString(threadId)
      } > 0
    }

  fun modify(threadId: String, modifyThreadRequest: ModifyThreadRequest): ThreadObject =
    transaction {
      val threadObject = get(threadId)
      val modifiedThreadObject = threadObject.copy(
        metadata = modifyThreadRequest.metadata
      )
      ThreadsTable.update({ ThreadsTable.id eq UUID.fromString(threadId) }) {
        it[data] = modifiedThreadObject
      }
      modifiedThreadObject
    }

}
