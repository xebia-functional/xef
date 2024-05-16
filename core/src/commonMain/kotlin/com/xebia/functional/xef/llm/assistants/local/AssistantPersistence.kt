package com.xebia.functional.xef.llm.assistants.local

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import kotlinx.serialization.json.JsonObject

object AssistantPersistence {

  interface Assistant {
    suspend fun get(assistantId: String): AssistantObject

    suspend fun create(createAssistantRequest: CreateAssistantRequest): AssistantObject

    suspend fun delete(assistantId: String): Boolean

    suspend fun list(
      limit: Int?,
      order: Assistants.OrderListAssistants?,
      after: String?,
      before: String?
    ): ListAssistantsResponse

    suspend fun modify(
      assistantId: String,
      modifyAssistantRequest: ModifyAssistantRequest
    ): AssistantObject
  }

  interface AssistantFiles {

    suspend fun create(
      assistantId: String,
      createAssistantFileRequest: CreateAssistantFileRequest
    ): AssistantFileObject

    suspend fun delete(assistantId: String, fileId: String): Boolean

    suspend fun get(assistantId: String, fileId: String): AssistantFileObject

    suspend fun list(
      assistantId: String,
      limit: Int?,
      order: Assistants.OrderListAssistantFiles?,
      after: String?,
      before: String?
    ): ListAssistantFilesResponse
  }

  interface Thread {
    suspend fun get(threadId: String): ThreadObject

    suspend fun delete(threadId: String): Boolean

    suspend fun create(
      assistantId: String?,
      runId: String?,
      createThreadRequest: CreateThreadRequest
    ): ThreadObject

    suspend fun modify(threadId: String, modifyThreadRequest: ModifyThreadRequest): ThreadObject
  }

  interface Message {

    suspend fun get(threadId: String, messageId: String): MessageObject

    suspend fun list(
      threadId: String,
      limit: Int?,
      order: Assistants.OrderListMessages?,
      after: String?,
      before: String?
    ): ListMessagesResponse

    suspend fun modify(
      threadId: String,
      messageId: String,
      modifyMessageRequest: ModifyMessageRequest
    ): MessageObject

    suspend fun createUserMessage(
      threadId: String,
      assistantId: String?,
      runId: String?,
      createMessageRequest: CreateMessageRequest
    ): MessageObject {
      val fileIds = emptyList<String>()
      val metadata = null
      val role = MessageObject.Role.user
      return createMessage(
        threadId,
        assistantId ?: "",
        runId ?: "",
        createMessageRequest.content,
        fileIds,
        metadata,
        role
      )
    }

    suspend fun createAssistantMessage(
      threadId: String,
      assistantId: String,
      runId: String,
      content: String
    ): MessageObject {
      val fileIds = emptyList<String>()
      val metadata = null
      val role = MessageObject.Role.assistant
      return createMessage(threadId, assistantId, runId, content, fileIds, metadata, role)
    }

    suspend fun createMessage(
      threadId: String,
      assistantId: String,
      runId: String,
      content: String,
      fileIds: List<String>,
      metadata: JsonObject?,
      role: MessageObject.Role
    ): MessageObject

    suspend fun updateContent(threadId: String, messageId: String, content: String): MessageObject
  }

  interface MessageFile {

    suspend fun get(threadId: String, messageId: String, fileId: String): MessageFileObject

    suspend fun list(
      threadId: String,
      messageId: String,
      limit: Int?,
      order: Assistants.OrderListMessageFiles?,
      after: String?,
      before: String?
    ): ListMessageFilesResponse
  }

  interface Step {

    suspend fun updateToolsStep(
      runObject: RunObject,
      id: String,
      stepCalls:
        List<RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject>
    ): RunStepObject

    suspend fun create(
      runObject: RunObject,
      choice: GeneralAssistants.AssistantDecision,
      toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>,
      messageId: String?
    ): RunStepObject

    suspend fun createToolsStep(
      runObject: RunObject,
      toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>
    ): RunStepObject =
      create(
        runObject = runObject,
        choice = GeneralAssistants.AssistantDecision.Tools,
        toolCalls = toolCalls,
        messageId = null
      )

    suspend fun createMessageStep(runObject: RunObject, messageId: String): RunStepObject =
      create(
        runObject = runObject,
        choice = GeneralAssistants.AssistantDecision.Message,
        toolCalls = emptyList(),
        messageId = messageId
      )

    suspend fun get(threadId: String, runId: String, stepId: String): RunStepObject

    suspend fun list(
      threadId: String,
      runId: String,
      limit: Int?,
      order: Assistants.OrderListRunSteps?,
      after: String?,
      before: String?
    ): ListRunStepsResponse
  }

  interface Run {

    suspend fun updateRunToRequireToolOutputs(
      runId: String,
      selectedTool: GeneralAssistants.SelectedTool
    ): RunObject

    suspend fun create(threadId: String, createRunRequest: CreateRunRequest): RunObject

    suspend fun list(
      threadId: String,
      limit: Int?,
      order: Assistants.OrderListRuns?,
      after: String?,
      before: String?
    ): ListRunsResponse

    suspend fun get(runId: String): RunObject

    suspend fun modify(runId: String, modifyRunRequest: ModifyRunRequest): RunObject
  }
}
