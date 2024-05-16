package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.model.CreateThreadRequest
import com.xebia.functional.openai.generated.model.ModifyThreadRequest
import com.xebia.functional.openai.generated.model.ThreadObject
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.threadObject
import com.xebia.functional.xef.server.db.tables.format
import java.util.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ThreadsTable : Table("xef_threads"), AssistantPersistence.Thread {
  val id = uuid("id")
  val data = jsonb<ThreadObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun create(
    assistantId: String?,
    runId: String?,
    createThreadRequest: CreateThreadRequest
  ): ThreadObject {
    val uuid = UUID.randomUUID()
    val threadObject = threadObject(kotlinx.uuid.UUID(uuid.toString()), createThreadRequest)
    transaction {
      ThreadsTable.insert {
        it[id] = uuid
        it[data] = threadObject
      }
    }
    createThreadRequest.messages.orEmpty().forEach {
      MessagesTable.createUserMessage(
        threadId = threadObject.id,
        assistantId = assistantId,
        runId = runId,
        createMessageRequest = it
      )
    }
    return threadObject
  }

  override suspend fun get(threadId: String): ThreadObject {
    return transaction {
      ThreadsTable.select(data)
        .where { ThreadsTable.id eq UUID.fromString(threadId) }
        .singleOrNull()
        ?.let { it[data] } ?: throw Exception("Thread not found for id: $threadId")
    }
  }

  override suspend fun delete(threadId: String): Boolean = transaction {
    ThreadsTable.deleteWhere { id eq UUID.fromString(threadId) } > 0
  }

  override suspend fun modify(
    threadId: String,
    modifyThreadRequest: ModifyThreadRequest
  ): ThreadObject {
    val threadObject = get(threadId)
    return transaction {
      val modifiedThreadObject = threadObject.copy(metadata = modifyThreadRequest.metadata)
      ThreadsTable.update({ ThreadsTable.id eq UUID.fromString(threadId) }) {
        it[data] = modifiedThreadObject
      }
      modifiedThreadObject
    }
  }
}
