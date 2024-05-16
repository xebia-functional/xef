package com.xebia.functional.xef.aws.bedrock

import com.xebia.functional.openai.ServerSentEvent
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow

class BedrockAssistants : Assistants {
  override suspend fun cancelRun(
    threadId: String,
    runId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override suspend fun createAssistant(
    createAssistantRequest: CreateAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject {
    TODO("Not yet implemented")
  }

  override suspend fun createAssistantFile(
    assistantId: String,
    createAssistantFileRequest: CreateAssistantFileRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantFileObject {
    TODO("Not yet implemented")
  }

  override suspend fun createMessage(
    threadId: String,
    createMessageRequest: CreateMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject {
    TODO("Not yet implemented")
  }

  override suspend fun createRun(
    threadId: String,
    createRunRequest: CreateRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override fun createRunStream(
    threadId: String,
    createRunRequest: CreateRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<ServerSentEvent> {
    TODO("Not yet implemented")
  }

  override suspend fun createThread(
    createThreadRequest: CreateThreadRequest?,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject {
    TODO("Not yet implemented")
  }

  override suspend fun createThreadAndRun(
    createThreadAndRunRequest: CreateThreadAndRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override fun createThreadAndRunStream(
    createThreadAndRunRequest: CreateThreadAndRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<ServerSentEvent> {
    TODO("Not yet implemented")
  }

  override suspend fun deleteAssistant(
    assistantId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantResponse {
    TODO("Not yet implemented")
  }

  override suspend fun deleteAssistantFile(
    assistantId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantFileResponse {
    TODO("Not yet implemented")
  }

  override suspend fun deleteThread(
    threadId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteThreadResponse {
    TODO("Not yet implemented")
  }

  override suspend fun getAssistant(
    assistantId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject {
    TODO("Not yet implemented")
  }

  override suspend fun getAssistantFile(
    assistantId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantFileObject {
    TODO("Not yet implemented")
  }

  override suspend fun getMessage(
    threadId: String,
    messageId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject {
    TODO("Not yet implemented")
  }

  override suspend fun getMessageFile(
    threadId: String,
    messageId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageFileObject {
    TODO("Not yet implemented")
  }

  override suspend fun getRun(
    threadId: String,
    runId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override suspend fun getRunStep(
    threadId: String,
    runId: String,
    stepId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunStepObject {
    TODO("Not yet implemented")
  }

  override suspend fun getThread(
    threadId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject {
    TODO("Not yet implemented")
  }

  override suspend fun listAssistantFiles(
    assistantId: String,
    limit: Int?,
    order: Assistants.OrderListAssistantFiles?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListAssistantFilesResponse {
    TODO("Not yet implemented")
  }

  override suspend fun listAssistants(
    limit: Int?,
    order: Assistants.OrderListAssistants?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListAssistantsResponse {
    TODO("Not yet implemented")
  }

  override suspend fun listMessageFiles(
    threadId: String,
    messageId: String,
    limit: Int?,
    order: Assistants.OrderListMessageFiles?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessageFilesResponse {
    TODO("Not yet implemented")
  }

  override suspend fun listMessages(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListMessages?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessagesResponse {
    TODO("Not yet implemented")
  }

  override suspend fun listRunSteps(
    threadId: String,
    runId: String,
    limit: Int?,
    order: Assistants.OrderListRunSteps?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListRunStepsResponse {
    TODO("Not yet implemented")
  }

  override suspend fun listRuns(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListRuns?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListRunsResponse {
    TODO("Not yet implemented")
  }

  override suspend fun modifyAssistant(
    assistantId: String,
    modifyAssistantRequest: ModifyAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject {
    TODO("Not yet implemented")
  }

  override suspend fun modifyMessage(
    threadId: String,
    messageId: String,
    modifyMessageRequest: ModifyMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject {
    TODO("Not yet implemented")
  }

  override suspend fun modifyRun(
    threadId: String,
    runId: String,
    modifyRunRequest: ModifyRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override suspend fun modifyThread(
    threadId: String,
    modifyThreadRequest: ModifyThreadRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject {
    TODO("Not yet implemented")
  }

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
}
