package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.CreateRunRequest
import com.xebia.functional.openai.generated.model.ListRunsResponse
import com.xebia.functional.openai.generated.model.ModifyRunRequest
import com.xebia.functional.openai.generated.model.RunObject
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.llm.assistants.local.GeneralAssistants
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.runObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.setRunToRequireToolOutouts
import com.xebia.functional.xef.server.db.tables.format
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object RunsTable : Table("xef_runs"), AssistantPersistence.Run {
  val id = uuid("id")
  val data = jsonb<RunObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun get(runId: String): RunObject = transaction {
    RunsTable.select(data)
      .where { RunsTable.id eq java.util.UUID.fromString(runId) }
      .singleOrNull()
      ?.let { it[data] } ?: throw Exception("Run not found for id: $runId")
  }

  override suspend fun create(threadId: String, request: CreateRunRequest): RunObject {
    val assistant = AssistantsTable.get(request.assistantId)
    return transaction {
      val uuid = java.util.UUID.randomUUID()
      val runObject = runObject(UUID(uuid.toString()), threadId, request, assistant)

      RunsTable.insert {
        it[id] = uuid
        it[data] = runObject
      }
      runObject
    }
  }

  override suspend fun list(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListRuns?,
    after: String?,
    before: String?
  ): ListRunsResponse {
    val query = RunsTable.select(data).where { data.extract<String>("threadId") eq threadId }
    val sortedQuery =
      when (order) {
        Assistants.OrderListRuns.asc ->
          query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
        Assistants.OrderListRuns.desc ->
          query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
        null -> query
      }
    val afterRun = after?.let { java.util.UUID.fromString(it) }
    val beforeRun = before?.let { java.util.UUID.fromString(it) }
    val afterRunIndex =
      afterRun?.let { sortedQuery.indexOfFirst { it[data].id == afterRun.toString() } }
    val beforeRunIndex =
      beforeRun?.let { sortedQuery.indexOfFirst { it[data].id == beforeRun.toString() } }
    val slicedQuery =
      when {
        afterRunIndex != null -> sortedQuery.drop(afterRunIndex + 1)
        beforeRunIndex != null -> sortedQuery.take(beforeRunIndex)
        else -> sortedQuery
      }
    val limitedQuery = limit?.let { slicedQuery.take(it) } ?: slicedQuery
    return ListRunsResponse(
      `object` = "list",
      data = limitedQuery.map { it[data] },
      firstId = limitedQuery.firstOrNull()?.get(data)?.id,
      lastId = limitedQuery.lastOrNull()?.get(data)?.id,
      hasMore = sortedQuery.count() > limitedQuery.count()
    )
  }

  override suspend fun modify(runId: String, modifyRunRequest: ModifyRunRequest): RunObject {
    val runObject = get(runId)
    return transaction {
      val modifiedRunObject =
        runObject.copy(metadata = modifyRunRequest.metadata ?: runObject.metadata)
      RunsTable.update({ RunsTable.id eq java.util.UUID.fromString(runId) }) {
        it[data] = modifiedRunObject
      }
      modifiedRunObject
    }
  }

  override suspend fun updateRunToRequireToolOutputs(
    id: String,
    selectedTools: GeneralAssistants.SelectedTool
  ): RunObject {
    val runObject = get(id)
    return transaction {
      val modifiedRunObject = setRunToRequireToolOutouts(runObject, selectedTools)
      RunsTable.update({ RunsTable.id eq java.util.UUID.fromString(id) }) {
        it[data] = modifiedRunObject
      }
      modifiedRunObject
    }
  }
}
