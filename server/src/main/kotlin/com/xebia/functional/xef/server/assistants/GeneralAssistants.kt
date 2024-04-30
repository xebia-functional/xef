package com.xebia.functional.xef.server.assistants

import com.xebia.functional.openai.ServerSentEvent
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.server.assistants.tables.*
import com.xebia.functional.xef.server.assistants.utils.RequestConversions.assistantObjectToolsInner
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow

class GeneralAssistants() : Assistants {

  //region Assistants

  override suspend fun getAssistant(assistantId: String, configure: HttpRequestBuilder.() -> Unit): AssistantObject =
    AssistantsTable.get(assistantId)

  override suspend fun createAssistant(
    createAssistantRequest: CreateAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject = AssistantsTable.create(createAssistantRequest)

  override suspend fun deleteAssistant(
    assistantId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantResponse {
    val deleted = AssistantsTable.delete(assistantId)
    return DeleteAssistantResponse(
      id = assistantId,
      deleted = deleted,
      `object` = DeleteAssistantResponse.Object.assistant_deleted
    )
  }

  override suspend fun listAssistants(
    limit: Int?,
    order: Assistants.OrderListAssistants?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListAssistantsResponse =
    AssistantsTable.list(limit, order, after, before)

  override suspend fun modifyAssistant(
    assistantId: String,
    modifyAssistantRequest: ModifyAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject =
    AssistantsTable.modify(assistantId, modifyAssistantRequest)

  //endregion

  //region Assistant files

  override suspend fun createAssistantFile(
    assistantId: String,
    createAssistantFileRequest: CreateAssistantFileRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantFileObject =
    AssistantsFilesTable.create(assistantId, createAssistantFileRequest)

  override suspend fun deleteAssistantFile(
    assistantId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantFileResponse {
    val deleted = AssistantsFilesTable.delete(assistantId, fileId)
    return DeleteAssistantFileResponse(
      id = fileId,
      deleted = deleted,
      `object` = DeleteAssistantFileResponse.Object.assistant_file_deleted
    )
  }

  override suspend fun getAssistantFile(
    assistantId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantFileObject =
    AssistantsFilesTable.get(assistantId, fileId)

  override suspend fun listAssistantFiles(
    assistantId: String,
    limit: Int?,
    order: Assistants.OrderListAssistantFiles?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListAssistantFilesResponse =
    AssistantsFilesTable.list(assistantId, limit, order, after, before)

  //endregion

  //region Threads

  override suspend fun getThread(threadId: String, configure: HttpRequestBuilder.() -> Unit): ThreadObject =
    ThreadsTable.get(threadId)

  override suspend fun deleteThread(threadId: String, configure: HttpRequestBuilder.() -> Unit): DeleteThreadResponse =
    DeleteThreadResponse(
      id = threadId,
      deleted = ThreadsTable.delete(threadId),
      `object` = DeleteThreadResponse.Object.thread_deleted
    )

  override suspend fun createThread(
    createThreadRequest: CreateThreadRequest?,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject =
    ThreadsTable.create(
      assistantId = null,
      runId = null, createThreadRequest = createThreadRequest ?: CreateThreadRequest()
    )

  override suspend fun modifyThread(
    threadId: String,
    modifyThreadRequest: ModifyThreadRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject =
    ThreadsTable.modify(threadId, modifyThreadRequest)

  override suspend fun createThreadAndRun(
    createThreadAndRunRequest: CreateThreadAndRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    val thread = createThread(createThreadAndRunRequest.thread)
    val run = createRun(
      thread.id, CreateRunRequest(
        assistantId = createThreadAndRunRequest.assistantId,
        instructions = createThreadAndRunRequest.instructions,
        tools = createThreadAndRunRequest.tools?.map { it.assistantObjectToolsInner() },
        metadata = createThreadAndRunRequest.metadata
      )
    )
    return run
  }

  override fun createThreadAndRunStream(
    createThreadAndRunRequest: CreateThreadAndRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<ServerSentEvent> {
    TODO("Not yet implemented")
  }

  //endregion

  //region Messages

  override suspend fun createMessage(
    threadId: String,
    createMessageRequest: CreateMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject =
    MessagesTable.create(
      threadId = threadId,
      assistantId = null,
      runId = null,
      createMessageRequest = createMessageRequest
    )

  override suspend fun getMessage(
    threadId: String,
    messageId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject =
    MessagesTable.get(threadId, messageId)

  override suspend fun listMessages(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListMessages?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessagesResponse =
    MessagesTable.list(threadId, limit, order, after, before)

  override suspend fun modifyMessage(
    threadId: String,
    messageId: String,
    modifyMessageRequest: ModifyMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject =
    MessagesTable.modify(threadId, messageId, modifyMessageRequest)

  //endregion

  //region Message files

  override suspend fun getMessageFile(
    threadId: String,
    messageId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageFileObject =
    MessagesFilesTable.get(threadId, messageId, fileId)

  override suspend fun listMessageFiles(
    threadId: String,
    messageId: String,
    limit: Int?,
    order: Assistants.OrderListMessageFiles?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessageFilesResponse =
    MessagesFilesTable.list(threadId, messageId, limit, order, after, before)

  //endregion

  //region Run

  override suspend fun createRun(
    threadId: String,
    createRunRequest: CreateRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject =
    RunsTable.create(threadId, createRunRequest).also {
      TODO("kick off the run which delegates to the createRunStream method.")
    }

  override suspend fun cancelRun(threadId: String, runId: String, configure: HttpRequestBuilder.() -> Unit): RunObject {
    TODO("Not yet implemented")
  }

  override suspend fun listRuns(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListRuns?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListRunsResponse =
    RunsTable.list(threadId, limit, order, after, before)

  override fun createRunStream(
    threadId: String,
    createRunRequest: CreateRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<ServerSentEvent> {
    TODO("Not yet implemented")
  }

  override suspend fun getRun(threadId: String, runId: String, configure: HttpRequestBuilder.() -> Unit): RunObject =
    RunsTable.get(runId)

  override suspend fun modifyRun(
    threadId: String,
    runId: String,
    modifyRunRequest: ModifyRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject =
    RunsTable.modify(runId, modifyRunRequest)

  //endregion

  //region Run steps

  override suspend fun getRunStep(
    threadId: String,
    runId: String,
    stepId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunStepObject =
    RunsStepsTable.get(threadId, runId, stepId)

  override suspend fun listRunSteps(
    threadId: String,
    runId: String,
    limit: Int?,
    order: Assistants.OrderListRunSteps?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListRunStepsResponse =
    RunsStepsTable.list(threadId, runId, limit, order, after, before)

  //endregion

  //region Submit tool outputs

  override suspend fun submitToolOuputsToRun(
    threadId: String,
    runId: String,
    submitToolOutputsRunRequest: SubmitToolOutputsRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override fun submitToolOuputsToRunStream(
    threadId: String,
    runId: String,
    submitToolOutputsRunRequest: SubmitToolOutputsRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<ServerSentEvent> {
    TODO("Not yet implemented")
  }

  //endregion
}
