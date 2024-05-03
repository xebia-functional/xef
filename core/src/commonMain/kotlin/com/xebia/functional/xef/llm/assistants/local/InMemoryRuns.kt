package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.CreateRunRequest
import com.xebia.functional.openai.generated.model.ListRunsResponse
import com.xebia.functional.openai.generated.model.ModifyRunRequest
import com.xebia.functional.openai.generated.model.RunObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class InMemoryRuns(private val assistants: AssistantPersistence.Assistant) :
  AssistantPersistence.Run {

  private val runs = Atomic.unsafe(emptyMap<UUID, RunObject>())

  override suspend fun updateRunToRequireToolOutputs(
    runId: String,
    selectedTool: GeneralAssistants.SelectedTool
  ): RunObject {
    val run = get(runId)
    val modifiedRun = AssistantUtils.setRunToRequireToolOutouts(run, selectedTool)
    runs.update { it + (UUID(runId) to modifiedRun) }
    return modifiedRun
  }

  override suspend fun create(threadId: String, createRunRequest: CreateRunRequest): RunObject {
    val uuid = UUID.generateUUID()
    val assistant = assistants.get(createRunRequest.assistantId)
    val runObject = AssistantUtils.runObject(uuid, threadId, createRunRequest, assistant)
    runs.update { it + (UUID(runObject.id) to runObject) }
    return runObject
  }

  override suspend fun list(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListRuns?,
    after: String?,
    before: String?
  ): ListRunsResponse {
    val allRuns = runs.get().values.toList()
    val sortedRuns =
      when (order) {
        Assistants.OrderListRuns.asc -> allRuns.sortedBy { it.createdAt }
        Assistants.OrderListRuns.desc -> allRuns.sortedByDescending { it.createdAt }
        null -> allRuns
      }
    val afterRun = after?.let { sortedRuns.indexOfFirst { it.id == after } }
    val beforeRun = before?.let { sortedRuns.indexOfFirst { it.id == before } }
    val runsToReturn =
      sortedRuns
        .let { afterRun?.let { afterIndex -> it.drop(afterIndex + 1) } ?: it }
        .let { beforeRun?.let { beforeIndex -> it.take(beforeIndex) } ?: it }
        .let { limit?.let { limit -> it.take(limit) } ?: it }
    return ListRunsResponse(
      `object` = "list",
      data = runsToReturn,
      firstId = runsToReturn.firstOrNull()?.id,
      lastId = runsToReturn.lastOrNull()?.id,
      hasMore = sortedRuns.size > runsToReturn.size
    )
  }

  override suspend fun get(runId: String): RunObject {
    return runs.get()[UUID(runId)] ?: throw Exception("Run not found for id: $runId")
  }

  override suspend fun modify(runId: String, modifyRunRequest: ModifyRunRequest): RunObject {
    val run = get(runId)
    val modifiedRun = run.copy(metadata = modifyRunRequest.metadata ?: run.metadata)
    runs.update { it + (UUID(runId) to modifiedRun) }
    return modifiedRun
  }
}
