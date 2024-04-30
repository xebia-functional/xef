package com.xebia.functional.xef.server.assistants.tables

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.ListRunStepsResponse
import com.xebia.functional.openai.generated.model.RunStepObject
import com.xebia.functional.xef.server.db.tables.format
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.transactions.transaction

object RunsStepsTable : Table("xef_runs") {

  val id = uuid("id")
  val data = jsonb<RunStepObject>("data", format)

  override val primaryKey = PrimaryKey(id)

  fun get(threadId: String, runId: String, stepId: String): RunStepObject =
    transaction {
      RunsStepsTable.select(
        data
      ).where {
        (RunsStepsTable.id eq java.util.UUID.fromString(stepId)) and
          (RunsStepsTable.data.extract<String>("runId") eq runId) and
          (RunsStepsTable.data.extract<String>("threadId") eq threadId)
      }.singleOrNull()?.let {
        it[data]
      } ?: throw Exception("Run step not found for id: $stepId")
    }

  fun list(
    threadId: String,
    runId: String,
    limit: Int?,
    order: Assistants.OrderListRunSteps?,
    after: String?,
    before: String?
  ): ListRunStepsResponse {
    return transaction {
      val query = RunsStepsTable.select(
        data
      ).where {
        (RunsStepsTable.data.extract<String>("runId") eq runId) and
          (RunsStepsTable.data.extract<String>("threadId") eq threadId)
      }
      val sortedQuery = when (order) {
        Assistants.OrderListRunSteps.asc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.ASC)
        Assistants.OrderListRunSteps.desc -> query.orderBy(data.extract<Int>("createdAt") to SortOrder.DESC)
        null -> query
      }
      val afterMessage = after?.let { java.util.UUID.fromString(it) }
      val beforeMessage = before?.let { java.util.UUID.fromString(it) }
      val afterMessageIndex = afterMessage?.let { sortedQuery.indexOfFirst { it[data].id == afterMessage.toString() } }
      val beforeMessageIndex = beforeMessage?.let { sortedQuery.indexOfFirst { it[data].id == beforeMessage.toString() } }
      val slicedQuery = when {
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

}
