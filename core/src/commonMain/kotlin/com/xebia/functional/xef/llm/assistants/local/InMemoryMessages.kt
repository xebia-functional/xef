package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.ListMessagesResponse
import com.xebia.functional.openai.generated.model.MessageObject
import com.xebia.functional.openai.generated.model.ModifyMessageRequest
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils
import kotlinx.serialization.json.JsonObject
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class InMemoryMessages : AssistantPersistence.Message {

  private val messages = Atomic.unsafe(emptyMap<UUID, MessageObject>())

  override suspend fun get(threadId: String, messageId: String): MessageObject {
    return messages.get()[UUID(messageId)]
      ?: throw Exception("Message not found for id: $messageId")
  }

  override suspend fun list(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListMessages?,
    after: String?,
    before: String?
  ): ListMessagesResponse {
    val allMessages = messages.get().values.toList()
    val sortedMessages =
      when (order) {
        Assistants.OrderListMessages.asc -> allMessages.sortedBy { it.createdAt }
        Assistants.OrderListMessages.desc -> allMessages.sortedByDescending { it.createdAt }
        null -> allMessages
      }
    val afterMessage = after?.let { sortedMessages.indexOfFirst { it.id == after } }
    val beforeMessage = before?.let { sortedMessages.indexOfFirst { it.id == before } }
    val messagesToReturn =
      sortedMessages
        .let { afterMessage?.let { afterIndex -> it.drop(afterIndex + 1) } ?: it }
        .let { beforeMessage?.let { beforeIndex -> it.take(beforeIndex) } ?: it }
        .let { limit?.let { limit -> it.take(limit) } ?: it }
    return ListMessagesResponse(
      `object` = "list",
      data = messagesToReturn,
      firstId = messagesToReturn.firstOrNull()?.id,
      lastId = messagesToReturn.lastOrNull()?.id,
      hasMore = sortedMessages.size > messagesToReturn.size
    )
  }

  override suspend fun modify(
    threadId: String,
    messageId: String,
    modifyMessageRequest: ModifyMessageRequest
  ): MessageObject {
    val message = get(threadId, messageId)
    val modifiedMessage = message.copy(metadata = modifyMessageRequest.metadata ?: message.metadata)
    messages.update { it + (UUID(messageId) to modifiedMessage) }
    return modifiedMessage
  }

  override suspend fun createMessage(
    threadId: String,
    assistantId: String,
    runId: String,
    content: String,
    fileIds: List<String>,
    metadata: JsonObject?,
    role: MessageObject.Role
  ): MessageObject {
    val uuid = UUID.generateUUID()
    val message =
      AssistantUtils.createMessageObject(
        uuid = uuid,
        threadId = threadId,
        assistantId = assistantId,
        runId = runId,
        content = content,
        fileIds = fileIds,
        metadata = metadata,
        role = role
      )
    messages.update { it + (UUID(message.id) to message) }
    return message
  }

  override suspend fun updateContent(
    threadId: String,
    messageId: String,
    content: String
  ): MessageObject {
    val message = get(threadId, messageId)
    val updatedMessage = AssistantUtils.modifiedMessageObject(message, content)
    messages.update { it + (UUID(messageId) to updatedMessage) }
    return updatedMessage
  }
}
