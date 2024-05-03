package com.xebia.functional.xef.server.assistants.utils

import arrow.fx.coroutines.timeInMillis
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.llm.assistants.local.GeneralAssistants
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

object AssistantUtils {

  fun threadObject(uuid: UUID, createThreadRequest: CreateThreadRequest): ThreadObject =
    ThreadObject(
      id = uuid.toString(),
      `object` = ThreadObject.Object.thread,
      createdAt = timeInMillis().toInt(),
      metadata = createThreadRequest.metadata
    )

  fun runObject(
    uuid: UUID,
    threadId: String,
    request: CreateRunRequest,
    assistant: AssistantObject
  ): RunObject =
    RunObject(
      id = uuid.toString(),
      `object` = RunObject.Object.thread_run,
      createdAt = timeInMillis().toInt(),
      threadId = threadId,
      assistantId = request.assistantId,
      status = RunObject.Status.in_progress,
      model = assistant.model,
      instructions = request.instructions ?: assistant.instructions ?: "",
      tools = request.tools ?: assistant.tools,
      fileIds = assistant.fileIds,
      metadata = request.metadata,
      usage = null,
      requiredAction = null,
      lastError = null,
      expiresAt = null,
      startedAt = timeInMillis().toInt(),
      cancelledAt = null,
      failedAt = null,
      completedAt = null
    )

  fun updatedRunStepObject(
    runStepObject: RunStepObject,
    stepCalls:
      List<RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject>
  ): RunStepObject =
    runStepObject.copy(
      status = RunStepObject.Status.completed,
      stepDetails =
        RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject(
          RunStepDetailsToolCallsObject(
            type = RunStepDetailsToolCallsObject.Type.tool_calls,
            toolCalls =
              stepCalls.map {
                RunStepDetailsToolCallsObjectToolCallsInner
                  .CaseRunStepDetailsToolCallsFunctionObject(
                    RunStepDetailsToolCallsFunctionObject(
                      id = it.value.id,
                      type = it.value.type,
                      function = it.value.function,
                    )
                  )
              }
          )
        )
    )

  fun modifiedMessageObject(messageObject: MessageObject, content: String): MessageObject =
    messageObject.copy(
      content =
        listOf(
          MessageObjectContentInner.CaseMessageContentTextObject(
            MessageContentTextObject(
              type = MessageContentTextObject.Type.text,
              text = MessageContentTextObjectText(value = content, annotations = emptyList())
            )
          )
        )
    )

  fun createMessageObject(
    uuid: UUID,
    threadId: String,
    role: MessageObject.Role,
    content: String,
    assistantId: String,
    runId: String,
    fileIds: List<String>,
    metadata: JsonObject?
  ): MessageObject =
    MessageObject(
      id = uuid.toString(),
      `object` = MessageObject.Object.thread_message,
      createdAt = timeInMillis().toInt(),
      threadId = threadId,
      role = role,
      content =
        listOf(
          MessageObjectContentInner.CaseMessageContentTextObject(
            MessageContentTextObject(
              type = MessageContentTextObject.Type.text,
              text = MessageContentTextObjectText(value = content, annotations = emptyList())
            )
          )
        ),
      assistantId = assistantId,
      runId = runId,
      fileIds = fileIds,
      metadata = metadata
    )

  fun modifiedAssistantObject(
    assistantObject: AssistantObject,
    modifyAssistantRequest: ModifyAssistantRequest
  ): AssistantObject =
    assistantObject.copy(
      name = modifyAssistantRequest.name ?: assistantObject.name,
      description = modifyAssistantRequest.description ?: assistantObject.description,
      model = modifyAssistantRequest.model ?: assistantObject.model,
      instructions = modifyAssistantRequest.instructions ?: assistantObject.instructions,
      tools = modifyAssistantRequest.tools ?: assistantObject.tools,
      fileIds = modifyAssistantRequest.fileIds ?: assistantObject.fileIds,
      metadata = modifyAssistantRequest.metadata ?: assistantObject.metadata
    )

  fun assistantObject(uuid: UUID, createAssistantRequest: CreateAssistantRequest): AssistantObject =
    AssistantObject(
      id = uuid.toString(),
      `object` = AssistantObject.Object.assistant,
      createdAt = timeInMillis().toInt(),
      name = createAssistantRequest.name,
      description = createAssistantRequest.description,
      model = createAssistantRequest.model,
      instructions = createAssistantRequest.instructions,
      tools = createAssistantRequest.tools.orEmpty(),
      fileIds = createAssistantRequest.fileIds.orEmpty(),
      metadata = createAssistantRequest.metadata
    )

  fun assistantFileObject(
    createAssistantFileRequest: CreateAssistantFileRequest,
    assistantId: String
  ): AssistantFileObject =
    AssistantFileObject(
      id = createAssistantFileRequest.fileId,
      `object` = AssistantFileObject.Object.assistant_file,
      createdAt = timeInMillis().toInt(),
      assistantId = assistantId,
    )

  fun CreateThreadAndRunRequestToolsInner.assistantObjectToolsInner(): AssistantObjectToolsInner =
    when (this) {
      is CreateThreadAndRunRequestToolsInner.CaseAssistantToolsCode ->
        AssistantObjectToolsInner.CaseAssistantToolsCode(
          AssistantToolsCode(AssistantToolsCode.Type.code_interpreter)
        )
      is CreateThreadAndRunRequestToolsInner.CaseAssistantToolsFunction ->
        AssistantObjectToolsInner.CaseAssistantToolsFunction(
          AssistantToolsFunction(
            type = AssistantToolsFunction.Type.function,
            function = value.function
          )
        )
      is CreateThreadAndRunRequestToolsInner.CaseAssistantToolsRetrieval ->
        AssistantObjectToolsInner.CaseAssistantToolsRetrieval(
          AssistantToolsRetrieval(AssistantToolsRetrieval.Type.retrieval)
        )
    }

  fun runStepObject(
    stepId: UUID,
    runObject: RunObject,
    choice: GeneralAssistants.AssistantDecision,
    toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>,
    messageId: String?
  ): RunStepObject =
    RunStepObject(
      id = stepId.toString(),
      `object` = RunStepObject.Object.thread_run_step,
      createdAt = (timeInMillis() / 1000).toInt(),
      assistantId = runObject.assistantId,
      threadId = runObject.threadId,
      runId = runObject.id,
      type =
        when (choice) {
          GeneralAssistants.AssistantDecision.Tools -> RunStepObject.Type.tool_calls
          GeneralAssistants.AssistantDecision.Message -> RunStepObject.Type.message_creation
        },
      status = RunStepObject.Status.in_progress,
      stepDetails =
        when (choice) {
          GeneralAssistants.AssistantDecision.Tools ->
            RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject(
              RunStepDetailsToolCallsObject(
                type = RunStepDetailsToolCallsObject.Type.tool_calls,
                toolCalls = toolCalls
              )
            )
          GeneralAssistants.AssistantDecision.Message ->
            RunStepObjectStepDetails.CaseRunStepDetailsMessageCreationObject(
              RunStepDetailsMessageCreationObject(
                type = RunStepDetailsMessageCreationObject.Type.message_creation,
                messageCreation =
                  RunStepDetailsMessageCreationObjectMessageCreation(
                    messageId =
                      messageId ?: error("Message ID is required for message creation step")
                  )
              )
            )
        },
      lastError = null,
      expiredAt = null,
      cancelledAt = null,
      failedAt = null,
      completedAt = null,
      metadata = null,
      usage = null
    )

  fun setRunToRequireToolOutouts(
    runObject: RunObject,
    selectedTool: GeneralAssistants.SelectedTool
  ): RunObject =
    runObject.copy(
      requiredAction =
        RunObjectRequiredAction(
          type = RunObjectRequiredAction.Type.submit_tool_outputs,
          submitToolOutputs =
            RunObjectRequiredActionSubmitToolOutputs(
              toolCalls =
                listOf(
                  RunToolCallObject(
                    id = UUID.generateUUID().toString(),
                    type = RunToolCallObject.Type.function,
                    function =
                      RunToolCallObjectFunction(
                        name = selectedTool.name,
                        arguments =
                          Json.encodeToString(JsonObject.serializer(), selectedTool.parameters)
                      )
                  )
                )
            )
        )
    )
}
