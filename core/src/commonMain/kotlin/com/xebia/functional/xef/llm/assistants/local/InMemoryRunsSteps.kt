package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.ListRunStepsResponse
import com.xebia.functional.openai.generated.model.RunObject
import com.xebia.functional.openai.generated.model.RunStepDetailsToolCallsObjectToolCallsInner
import com.xebia.functional.openai.generated.model.RunStepObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class InMemoryRunsSteps : AssistantPersistence.Step {

  private val steps = Atomic.unsafe(emptyMap<UUID, RunStepObject>())

  override suspend fun updateToolsStep(
    runObject: RunObject,
    id: String,
    stepCalls:
      List<RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject>
  ): RunStepObject {
    val uuid = UUID(id)
    val step = get(runObject.threadId, runObject.id, id)
    val updatedStep = AssistantUtils.updatedRunStepObject(step, stepCalls)
    steps.update { it + (uuid to updatedStep) }
    return updatedStep
  }

  override suspend fun create(
    runObject: RunObject,
    choice: GeneralAssistants.AssistantDecision,
    toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>,
    messageId: String?
  ): RunStepObject {
    val stepId = UUID.generateUUID()
    val stepObject = AssistantUtils.runStepObject(stepId, runObject, choice, toolCalls, messageId)
    steps.update { it + (stepId to stepObject) }
    return stepObject
  }

  override suspend fun get(threadId: String, runId: String, stepId: String): RunStepObject {
    return steps.get()[UUID(stepId)] ?: throw Exception("Step not found for id: $stepId")
  }

  override suspend fun list(
    threadId: String,
    runId: String,
    limit: Int?,
    order: Assistants.OrderListRunSteps?,
    after: String?,
    before: String?
  ): ListRunStepsResponse {
    TODO("Not yet implemented")
  }
}
