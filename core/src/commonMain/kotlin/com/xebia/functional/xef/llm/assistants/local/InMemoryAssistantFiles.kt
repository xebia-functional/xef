package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.AssistantFileObject
import com.xebia.functional.openai.generated.model.CreateAssistantFileRequest
import com.xebia.functional.openai.generated.model.ListAssistantFilesResponse
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class InMemoryAssistantFiles : AssistantPersistence.AssistantFiles {
  private val assistantFiles = Atomic.unsafe(emptyMap<UUID, AssistantFileObject>())

  override suspend fun create(
    assistantId: String,
    createAssistantFileRequest: CreateAssistantFileRequest
  ): AssistantFileObject {
    val uuid = UUID.generateUUID()
    val assistantFileObject =
      AssistantUtils.assistantFileObject(createAssistantFileRequest, assistantId)
    assistantFiles.update { it + (uuid to assistantFileObject) }
    return assistantFileObject
  }

  override suspend fun get(assistantId: String, fileId: String): AssistantFileObject =
    assistantFiles.get().values.firstOrNull { it.id == fileId }
      ?: throw Exception("Assistant file not found for id: $fileId")

  override suspend fun delete(assistantId: String, fileId: String): Boolean =
    assistantFiles
      .updateAndGet { it.filter { (_, assistantFile) -> assistantFile.id != fileId } }
      .isNotEmpty()

  override suspend fun list(
    assistantId: String,
    limit: Int?,
    order: Assistants.OrderListAssistantFiles?,
    after: String?,
    before: String?
  ): ListAssistantFilesResponse {
    val allAssistantFiles = assistantFiles.get().values.toList()
    val sortedAssistantFiles =
      when (order) {
        Assistants.OrderListAssistantFiles.asc -> allAssistantFiles.sortedBy { it.createdAt }
        Assistants.OrderListAssistantFiles.desc ->
          allAssistantFiles.sortedByDescending { it.createdAt }
        null -> allAssistantFiles
      }
    val afterAssistantFile = after?.let { sortedAssistantFiles.indexOfFirst { it.id == after } }
    val beforeAssistantFile = before?.let { sortedAssistantFiles.indexOfFirst { it.id == before } }
    val assistantFilesToReturn =
      sortedAssistantFiles
        .let { afterAssistantFile?.let { afterIndex -> it.drop(afterIndex + 1) } ?: it }
        .let { beforeAssistantFile?.let { beforeIndex -> it.take(beforeIndex) } ?: it }
        .let { limit?.let { limit -> it.take(limit) } ?: it }
    return ListAssistantFilesResponse(
      `object` = "list",
      data = assistantFilesToReturn,
      firstId = assistantFilesToReturn.firstOrNull()?.id,
      lastId = assistantFilesToReturn.lastOrNull()?.id,
      hasMore = sortedAssistantFiles.size > assistantFilesToReturn.size
    )
  }
}
