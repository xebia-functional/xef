package com.xebia.functional.xef.server.assistants.postgres.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.llm.assistants.local.AssistantPersistence
import com.xebia.functional.xef.llm.assistants.local.GeneralAssistants
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.runStepObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.updatedRunStepObject
import com.xebia.functional.xef.server.db.tables.format
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction

object RunsStepsTable : Table("xef_runs"), AssistantPersistence.Step {

  val id = uuid("id")
  val data = jsonb<RunStepObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  override suspend fun get(threadId: String, runId: String, stepId: String): RunStepObject =
    transaction {
      RunsStepsTable.select(data)
        .where {
          (RunsStepsTable.id eq java.util.UUID.fromString(stepId)) and
            (data.extract<String>("runId") eq runId) and
            (data.extract<String>("threadId") eq threadId)
        }
        .singleOrNull()
        ?.let { it[data] } ?: throw Exception("Run step not found for id: $stepId")
    }

  override suspend fun list(
    threadId: String,
    runId: String,
    limit: Int?,
    order: Assistants.OrderListRunSteps?,
    after: String?,
    before: String?
  ): ListRunStepsResponse {
    return transaction {
      val query =
        RunsStepsTable.select(data).where {
          (data.extract<String>("runId") eq runId) and
            (data.extract<String>("threadId") eq threadId)
        }
      val sortedQuery =
        when (order) {
          Assistants.OrderListRunSteps.asc ->
            query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
          Assistants.OrderListRunSteps.desc ->
            query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
          null -> query
        }
      val afterMessage = after?.let { java.util.UUID.fromString(it) }
      val beforeMessage = before?.let { java.util.UUID.fromString(it) }
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
      ListRunStepsResponse(
        `object` = "list",
        data = limitedQuery.map { it[data] },
        firstId = limitedQuery.firstOrNull()?.get(data)?.id,
        lastId = limitedQuery.lastOrNull()?.get(data)?.id,
        hasMore = sortedQuery.count() > limitedQuery.count()
      )
    }
  }

  override suspend fun create(
    runObject: RunObject,
    choice: GeneralAssistants.AssistantDecision,
    toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>,
    messageId: String?
  ): RunStepObject {
    return transaction {
      val stepId = UUID.generateUUID()
      val runStepObject = runStepObject(stepId, runObject, choice, toolCalls, messageId)
      RunsStepsTable.insert {
        it[id] = java.util.UUID.fromString(stepId.toString())
        it[data] = runStepObject
      }
      runStepObject
    }
  }

  suspend fun updateStatus(
    threadId: String,
    runId: String,
    stepId: String,
    status: RunStepObject.Status
  ): RunStepObject {
    val runStepObject = get(threadId, runId, stepId)
    return transaction {
      val updatedRunStepObject = runStepObject.copy(status = status)
      RunsStepsTable.update({ RunsStepsTable.id eq java.util.UUID.fromString(stepId) }) {
        it[data] = updatedRunStepObject
      }
      updatedRunStepObject
    }
  }

  override suspend fun updateToolsStep(
    runObject: RunObject,
    stepId: String,
    stepCalls:
      List<RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject>
  ): RunStepObject {
    val runStepObject = get(runObject.threadId, runObject.id, stepId)
    return transaction {
      val updatedRunStepObject = updatedRunStepObject(runStepObject, stepCalls)
      RunsStepsTable.update({ RunsStepsTable.id eq java.util.UUID.fromString(stepId) }) {
        it[data] = updatedRunStepObject
      }
      updatedRunStepObject
    }
  }
}
