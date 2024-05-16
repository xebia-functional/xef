package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.ListMessageFilesResponse
import com.xebia.functional.openai.generated.model.MessageFileObject
import kotlinx.uuid.UUID

class InMemoryMessagesFiles : AssistantPersistence.MessageFile {

  private val messagesFiles = Atomic.unsafe(emptyMap<UUID, MessageFileObject>())

  override suspend fun get(threadId: String, messageId: String, fileId: String): MessageFileObject {
    return messagesFiles.get()[UUID(fileId)]
      ?: throw Exception("Message file not found for id: $fileId")
  }

  override suspend fun list(
    threadId: String,
    messageId: String,
    limit: Int?,
    order: Assistants.OrderListMessageFiles?,
    after: String?,
    before: String?
  ): ListMessageFilesResponse {
    val allMessageFiles = messagesFiles.get().values.toList()
    val sortedMessageFiles =
      when (order) {
        Assistants.OrderListMessageFiles.asc -> allMessageFiles.sortedBy { it.createdAt }
        Assistants.OrderListMessageFiles.desc -> allMessageFiles.sortedByDescending { it.createdAt }
        null -> allMessageFiles
      }
    val afterMessageFile = after?.let { sortedMessageFiles.indexOfFirst { it.id == after } }
    val beforeMessageFile = before?.let { sortedMessageFiles.indexOfFirst { it.id == before } }
    val messageFilesToReturn =
      sortedMessageFiles
        .let { afterMessageFile?.let { afterIndex -> it.drop(afterIndex + 1) } ?: it }
        .let { beforeMessageFile?.let { beforeIndex -> it.take(beforeIndex) } ?: it }
        .let { limit?.let { limit -> it.take(limit) } ?: it }
    return ListMessageFilesResponse(
      `object` = "list",
      data = messageFilesToReturn,
      firstId = messageFilesToReturn.firstOrNull()?.id,
      lastId = messageFilesToReturn.lastOrNull()?.id,
      hasMore = sortedMessageFiles.size > messageFilesToReturn.size
    )
  }
}
