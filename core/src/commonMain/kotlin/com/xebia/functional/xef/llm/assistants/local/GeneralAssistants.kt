package com.xebia.functional.xef.llm.assistants.local

import com.xebia.functional.openai.ServerSentEvent
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.MessagePolicy
import com.xebia.functional.xef.llm.PromptCalculator.adaptPromptToConversationAndModel
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.llm.assistants.RunDelta.MessageDeltaObject
import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.assistant
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.ToolCallStrategy
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils.assistantObjectToolsInner
import io.ktor.client.request.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * @param parent an optional parent Job. If a parent job is passed, cancelling the parent job will
 *   cancel all launched coroutines by [GeneralAssistants]. This is useful when you want to couple
 *   the lifecycle of Spring, Ktor, Android, or any other framework to [GeneralAssistants]. When any
 *   of them close, so will all launched jobs.
 */
class GeneralAssistants(
  private val api: Chat,
  private val assistantPersistence: AssistantPersistence.Assistant,
  private val assistantFilesPersistence: AssistantPersistence.AssistantFiles,
  private val threadPersistence: AssistantPersistence.Thread,
  private val messagePersistence: AssistantPersistence.Message,
  private val messageFilesPersistence: AssistantPersistence.MessageFile,
  private val runPersistence: AssistantPersistence.Run,
  private val runStepPersistence: AssistantPersistence.Step,
  context: CoroutineContext = EmptyCoroutineContext,
  parent: Job? = null
) : Assistants {
  private val supervisor = SupervisorJob(parent)
  private val scope = CoroutineScope(context + supervisor)

  // region Assistants

  override suspend fun getAssistant(
    assistantId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject = assistantPersistence.get(assistantId)

  override suspend fun createAssistant(
    createAssistantRequest: CreateAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject = assistantPersistence.create(createAssistantRequest)

  override suspend fun deleteAssistant(
    assistantId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantResponse {
    val deleted = assistantPersistence.delete(assistantId)
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
  ): ListAssistantsResponse = assistantPersistence.list(limit, order, after, before)

  override suspend fun modifyAssistant(
    assistantId: String,
    modifyAssistantRequest: ModifyAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject = assistantPersistence.modify(assistantId, modifyAssistantRequest)

  // endregion

  // region Assistant files

  override suspend fun createAssistantFile(
    assistantId: String,
    createAssistantFileRequest: CreateAssistantFileRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantFileObject = assistantFilesPersistence.create(assistantId, createAssistantFileRequest)

  override suspend fun deleteAssistantFile(
    assistantId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantFileResponse {
    val deleted = assistantFilesPersistence.delete(assistantId, fileId)
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
  ): AssistantFileObject = assistantFilesPersistence.get(assistantId, fileId)

  override suspend fun listAssistantFiles(
    assistantId: String,
    limit: Int?,
    order: Assistants.OrderListAssistantFiles?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListAssistantFilesResponse =
    assistantFilesPersistence.list(assistantId, limit, order, after, before)

  // endregion

  // region Threads

  override suspend fun getThread(
    threadId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject = threadPersistence.get(threadId)

  override suspend fun deleteThread(
    threadId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteThreadResponse =
    DeleteThreadResponse(
      id = threadId,
      deleted = threadPersistence.delete(threadId),
      `object` = DeleteThreadResponse.Object.thread_deleted
    )

  override suspend fun createThread(
    createThreadRequest: CreateThreadRequest?,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject =
    threadPersistence.create(
      assistantId = null,
      runId = null,
      createThreadRequest = createThreadRequest ?: CreateThreadRequest()
    )

  override suspend fun modifyThread(
    threadId: String,
    modifyThreadRequest: ModifyThreadRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject = threadPersistence.modify(threadId, modifyThreadRequest)

  override suspend fun createThreadAndRun(
    createThreadAndRunRequest: CreateThreadAndRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    val thread = createThread(createThreadAndRunRequest.thread)
    val run =
      createRun(
        thread.id,
        CreateRunRequest(
          assistantId = createThreadAndRunRequest.assistantId,
          instructions = createThreadAndRunRequest.instructions,
          tools = createThreadAndRunRequest.tools?.map { it.assistantObjectToolsInner() },
          metadata = createThreadAndRunRequest.metadata,
          model = createThreadAndRunRequest.model,
          additionalInstructions = createThreadAndRunRequest.instructions
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

  // endregion

  // region Messages

  override suspend fun createMessage(
    threadId: String,
    createMessageRequest: CreateMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject =
    messagePersistence.createUserMessage(
      threadId = threadId,
      assistantId = null,
      runId = null,
      createMessageRequest = createMessageRequest
    )

  override suspend fun getMessage(
    threadId: String,
    messageId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject = messagePersistence.get(threadId, messageId)

  override suspend fun listMessages(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListMessages?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessagesResponse = messagePersistence.list(threadId, limit, order, after, before)

  override suspend fun modifyMessage(
    threadId: String,
    messageId: String,
    modifyMessageRequest: ModifyMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject = messagePersistence.modify(threadId, messageId, modifyMessageRequest)

  // endregion

  // region Message files

  override suspend fun getMessageFile(
    threadId: String,
    messageId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageFileObject = messageFilesPersistence.get(threadId, messageId, fileId)

  override suspend fun listMessageFiles(
    threadId: String,
    messageId: String,
    limit: Int?,
    order: Assistants.OrderListMessageFiles?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessageFilesResponse =
    messageFilesPersistence.list(threadId, messageId, limit, order, after, before)

  // endregion

  // region Run

  @Description("The decision to run a tool or provide a message depending on th `context`")
  @Serializable
  data class ToolsOrMessageDecision(
    @Required
    @Description("Set `tool = 1, message = 0` if `context` requires a tool")
    val tool: Int,
    @Required
    @Description("Set `tool = 0, message = 1` if `context` requires a reply message")
    val message: Int,
    @Required
    @Description(
      "A short statement describing the reason why you made the choice of tool or reply message."
    )
    val reason: String
  )

  enum class AssistantDecision {
    Tools,
    Message;

    companion object {
      fun fromToolsOrMessageDecision(value: ToolsOrMessageDecision): AssistantDecision {
        println("Decision: ${value.reason}")
        return when {
          value.tool == 1 -> Tools
          value.message == 1 -> Message
          else -> throw IllegalArgumentException("Invalid value: $value")
        }
      }
    }
  }

  private fun createPrompt(
    runObject: RunObject,
    messages: List<MessageObject>,
    functions: List<FunctionObject> = emptyList()
  ): Prompt =
    Prompt(
      model = CreateChatCompletionRequestModel.Custom(runObject.model),
      functions = functions,
      configuration =
        PromptConfiguration {
          messagePolicy = MessagePolicy(historyPercent = 100, contextPercent = 0)
        }
    ) {
      +system(runObject.instructions)
      +createToolsMessages(runObject.tools)
      +createPromptMessages(messages)
    }

  private fun createToolsMessages(
    tools: List<AssistantObjectToolsInner>
  ): List<ChatCompletionRequestMessage> =
    tools.mapNotNull { tool ->
      when (tool) {
        is AssistantObjectToolsInner.CaseAssistantToolsCode ->
          null // TODO implement with sandbox python environment
        is AssistantObjectToolsInner.CaseAssistantToolsFunction ->
          assistant(
            """
            |<available-tool>
            |Function: ${tool.value.function.name}
            |Description: ${tool.value.function.description}
            |<schema>
            |${tool.value.function.parameters?.let { Json.encodeToString(JsonObject.serializer(), it) }}
            |</schema>
            |</available-tool>
          """
              .trimMargin()
          )
        is AssistantObjectToolsInner.CaseAssistantToolsRetrieval ->
          null // TODO implement with vector store
      }
    }

  private fun createPromptMessages(
    messages: List<MessageObject>
  ): List<ChatCompletionRequestMessage> =
    messages.flatMap { msg ->
      msg.content.map { content ->
        when (content) {
          is MessageObjectContentInner.CaseMessageContentImageFileObject ->
            when (msg.role) {
              MessageObject.Role.user -> user("Image: ${content.value.imageFile.fileId}")
              MessageObject.Role.assistant -> assistant("Image: ${content.value.imageFile.fileId}")
            }
          is MessageObjectContentInner.CaseMessageContentTextObject ->
            when (msg.role) {
              MessageObject.Role.user -> user(content.value.text.value)
              MessageObject.Role.assistant -> assistant(content.value.text.value)
            }
        }
      }
    }

  @Serializable
  data class SelectedTool(
    @Description("the name of the tool to run") val name: String,
    @Description(
      "the arguments to pass to the tool, expressed as a json object in a single line string, arguments name extracted from tool JSON schema. Example: {\"argName1\": \"value1\", \"argName2\": \"value2\"}"
    )
    val parameters: JsonObject
  )

  private suspend fun ProducerScope<RunDelta>.processRun(runObject: RunObject) {
    // notify that the run is in progress
    send(RunDelta.RunInProgress(runObject))
    val thread = getThread(runObject.threadId)
    val messages = listMessages(thread.id, limit = 1, order = Assistants.OrderListMessages.desc)
    val decisionPrompt = decisionPrompt(runObject, messages)
    val decision = AI<ToolsOrMessageDecision>(prompt = decisionPrompt, api = api)
    val choice = AssistantDecision.fromToolsOrMessageDecision(decision)
    val prompt: Prompt = createPrompt(runObject, messages.data)
    when (choice) {
      AssistantDecision.Tools -> {
        val toolsRunStep = runStepPersistence.createToolsStep(runObject, emptyList())
        send(RunDelta.RunStepInProgress(toolsRunStep))
        val selectedTool = toolCalls(prompt, runObject)
        val stepCalls = listOf(toolCallsToStepDetails(selectedTool))
        val updatedStep = runStepPersistence.updateToolsStep(runObject, toolsRunStep.id, stepCalls)
        send(RunDelta.RunStepCompleted(updatedStep))
        val updatedRun = runPersistence.updateRunToRequireToolOutputs(runObject.id, selectedTool)
        send(RunDelta.RunRequiresAction(updatedRun))
      }
      AssistantDecision.Message -> {
        val message =
          messagePersistence.createAssistantMessage(
            threadId = thread.id,
            assistantId = runObject.assistantId,
            runId = runObject.id,
            content = ""
          )
        val createMessageRunStep = runStepPersistence.createMessageStep(runObject, message.id)
        send(RunDelta.RunStepCreated(createMessageRunStep))
        send(RunDelta.MessageInProgress(message))
        val content = StringBuilder()
        AI<Flow<String>>(prompt = prompt, api = api).collect { partialDelta ->
          content.append(partialDelta)
          send(messageDelta(message, partialDelta))
        }
        val completedMessage =
          messagePersistence.updateContent(thread.id, message.id, content.toString())
        send(RunDelta.MessageCompleted(completedMessage))
        send(RunDelta.RunCompleted(runObject))
      }
    }
  }

  private suspend fun decisionPrompt(runObject: RunObject, messages: ListMessagesResponse): Prompt =
    Prompt(
        functions = listOf(chatFunction(ToolsOrMessageDecision.serializer().descriptor)),
        model = CreateChatCompletionRequestModel.Custom(runObject.model),
        toolCallStrategy = runObject.toolCallStrategy()
      ) {
        +createToolsMessages(runObject.tools)
        +createPromptMessages(messages.data)
        +user("Please select the tool you would like to run or provide a message.")
      }
      .adaptPromptToConversationAndModel(Conversation())

  private fun toolCallsToStepDetails(call: SelectedTool) =
    RunStepDetailsToolCallsObjectToolCallsInner.CaseRunStepDetailsToolCallsFunctionObject(
      RunStepDetailsToolCallsFunctionObject(
        type = RunStepDetailsToolCallsFunctionObject.Type.function,
        function =
          RunStepDetailsToolCallsFunctionObjectFunction(
            name = call.name,
            arguments =
              Config.DEFAULT.json.encodeToString(JsonObject.serializer(), call.parameters),
            output = null
          )
      )
    )

  private suspend fun toolCalls(
    currentConversationPrompt: Prompt,
    runObject: RunObject
  ): SelectedTool {
    val functions = functionObjects(runObject)
    return AI<SelectedTool>(
      prompt = selectToolPrompt(functions, runObject, currentConversationPrompt),
      api = api
    )
  }

  private fun RunObject.toolCallStrategy(): ToolCallStrategy =
    metadata?.get(ToolCallStrategy.Key)?.let {
      when (it) {
        is JsonPrimitive -> it.contentOrNull?.let { ToolCallStrategy.valueOf(it) }
        else -> null
      }
    } ?: ToolCallStrategy.Supported

  private fun selectToolPrompt(
    functions: List<FunctionObject>,
    runObject: RunObject,
    currentConversationPrompt: Prompt
  ): Prompt =
    Prompt(
      functions = functions,
      model = CreateChatCompletionRequestModel.Custom(runObject.model),
      configuration =
        PromptConfiguration {
          maxTokens = 1000
          messagePolicy = MessagePolicy(historyPercent = 100, contextPercent = 0)
        },
      toolCallStrategy = runObject.toolCallStrategy()
    ) {
      +currentConversationPrompt
      runObject.tools.forEach { tool ->
        when (tool) {
          is AssistantObjectToolsInner.CaseAssistantToolsCode -> {
            // TODO implement with sandbox python environment
          }
          is AssistantObjectToolsInner.CaseAssistantToolsFunction -> {
            +assistant(
              """
                        |Function: ${tool.value.function.name}
                        |Description: ${tool.value.function.description}
                        |<arguments-schema>
                        |${tool.value.function.parameters?.let { Json.encodeToString(JsonObject.serializer(), it) }}
                        |</arguments-schema>
                        |
                      """
                .trimMargin()
            )
          }
          is AssistantObjectToolsInner.CaseAssistantToolsRetrieval -> {
            // TODO implement with vector store
          }
        }
      }
      +assistant(
        "Please provide the tool you would like to run to answer the user question. Respond in one line with the tool name and arguments and don't use \\n."
      )
    }

  private fun functionObjects(runObject: RunObject) =
    runObject.tools.mapNotNull { tool ->
      when (tool) {
        is AssistantObjectToolsInner.CaseAssistantToolsCode ->
          null // TODO implement with sandbox python environment
        is AssistantObjectToolsInner.CaseAssistantToolsFunction -> tool.value.function
        is AssistantObjectToolsInner.CaseAssistantToolsRetrieval ->
          null // TODO implement with vector store
      }
    }

  private fun messageDelta(message: MessageObject, partialDelta: String): RunDelta.MessageDelta =
    RunDelta.MessageDelta(
      MessageDeltaObject(
        id = message.id,
        `object` = "message_delta",
        delta =
          RunDelta.MessageDeltaObjectInner(
            content =
              listOf(
                RunDelta.MessageDeltaObjectInnerContent(
                  index = 0,
                  type = "text",
                  text = RunDelta.MessageDeltaObjectInnerContentText(value = partialDelta)
                )
              )
          )
      )
    )

  override suspend fun createRun(
    threadId: String,
    createRunRequest: CreateRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject = runPersistence.create(threadId, createRunRequest)

  //      .also { runObject ->
  //      // We remove the parent, such that our scope Job doesn't get overridden
  //      // This way we inherit the dispatcher, and context from createRun but run on our scope.
  //      val context = currentCoroutineContext().minusKey(Job)
  //      scope.launch(context) {
  //        channelFlow { processRun(runObject) }.singleOrNull()
  //      }
  //    }

  override suspend fun cancelRun(
    threadId: String,
    runId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject {
    TODO("Not yet implemented")
  }

  override suspend fun listRuns(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListRuns?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListRunsResponse = runPersistence.list(threadId, limit, order, after, before)

  override fun createRunStream(
    threadId: String,
    createRunRequest: CreateRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<ServerSentEvent> =
    channelFlow {
        val thread = threadPersistence.get(threadId)
        val run = createRun(thread.id, createRunRequest)
        processRun(run)
      }
      .mapNotNull { RunDelta.toServerSentEvent(it) }

  override suspend fun getRun(
    threadId: String,
    runId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject = runPersistence.get(runId)

  override suspend fun modifyRun(
    threadId: String,
    runId: String,
    modifyRunRequest: ModifyRunRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): RunObject = runPersistence.modify(runId, modifyRunRequest)

  // endregion

  // region Run steps

  override suspend fun getRunStep(
    threadId: String,
    runId: String,
    stepId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): RunStepObject = runStepPersistence.get(threadId, runId, stepId)

  override suspend fun listRunSteps(
    threadId: String,
    runId: String,
    limit: Int?,
    order: Assistants.OrderListRunSteps?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListRunStepsResponse = runStepPersistence.list(threadId, runId, limit, order, after, before)

  // endregion

  // region Submit tool outputs

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

  // endregion

  // Guarantee backpressure on cancellation
  //  override fun close() = runBlocking {
  //    supervisor.cancelAndJoin()
  //  }
}
