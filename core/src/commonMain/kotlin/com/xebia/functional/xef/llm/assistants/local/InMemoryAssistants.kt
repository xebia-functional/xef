package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.AssistantObject
import com.xebia.functional.openai.generated.model.CreateAssistantRequest
import com.xebia.functional.openai.generated.model.ListAssistantsResponse
import com.xebia.functional.openai.generated.model.ModifyAssistantRequest
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class InMemoryAssistants : AssistantPersistence.Assistant {
  private val assistants = Atomic.unsafe(emptyMap<UUID, AssistantObject>())

  override suspend fun create(createAssistantRequest: CreateAssistantRequest): AssistantObject {
    val uuid = UUID.generateUUID()
    val assistantObject = AssistantUtils.assistantObject(uuid, createAssistantRequest)
    assistants.update { it + (uuid to assistantObject) }
    return assistantObject
  }

  override suspend fun get(assistantId: String): AssistantObject =
    assistants.get().values.firstOrNull { it.id == assistantId }
      ?: throw Exception("Assistant not found for id: $assistantId")

  override suspend fun delete(assistantId: String): Boolean =
    assistants
      .updateAndGet { it.filter { (_, assistant) -> assistant.id != assistantId } }
      .isNotEmpty()

  override suspend fun list(
    limit: Int?,
    order: Assistants.OrderListAssistants?,
    after: String?,
    before: String?
  ): ListAssistantsResponse {
    val allAssistants = assistants.get().values.toList()
    val sortedAssistants =
      when (order) {
        Assistants.OrderListAssistants.asc -> allAssistants.sortedBy { it.createdAt }
        Assistants.OrderListAssistants.desc -> allAssistants.sortedByDescending { it.createdAt }
        null -> allAssistants
      }
    val afterAssistant = after?.let { sortedAssistants.indexOfFirst { it.id == after } }
    val beforeAssistant = before?.let { sortedAssistants.indexOfFirst { it.id == before } }
    val assistantsToReturn =
      sortedAssistants
        .let { afterAssistant?.let { afterIndex -> it.drop(afterIndex + 1) } ?: it }
        .let { beforeAssistant?.let { beforeIndex -> it.take(beforeIndex) } ?: it }
        .let { limit?.let { limit -> it.take(limit) } ?: it }
    return ListAssistantsResponse(
      `object` = "list",
      data = assistantsToReturn,
      firstId = assistantsToReturn.firstOrNull()?.id,
      lastId = assistantsToReturn.lastOrNull()?.id,
      hasMore = sortedAssistants.size > assistantsToReturn.size
    )
  }

  override suspend fun modify(
    assistantId: String,
    modifyAssistantRequest: ModifyAssistantRequest
  ): AssistantObject {
    val assistant = get(assistantId)
    val modifiedAssistant =
      AssistantUtils.modifiedAssistantObject(assistant, modifyAssistantRequest)
    assistants.update { it + (UUID(assistant.id) to modifiedAssistant) }
    return modifiedAssistant
  }

  companion object {
    operator fun invoke(api: Chat): Assistants {
      val assistants = InMemoryAssistants()
      val assistantFiles = InMemoryAssistantFiles()
      val threads = InMemoryThreads()
      val messages = InMemoryMessages()
      val messageFiles = InMemoryMessagesFiles()
      val runs = InMemoryRuns(assistants)
      val runsSteps = InMemoryRunsSteps()
      return GeneralAssistants(
        api = api,
        assistantPersistence = assistants,
        assistantFilesPersistence = assistantFiles,
        threadPersistence = threads,
        messagePersistence = messages,
        messageFilesPersistence = messageFiles,
        runPersistence = runs,
        runStepPersistence = runsSteps
      )
    }
  }
}
