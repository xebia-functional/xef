package com.xebia.functional.xef.store

import com.xebia.functional.openai.ServerSentEvent
import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.llm.assistants.AssistantRequest
import com.xebia.functional.xef.llm.assistants.AssistantTool
import com.xebia.functional.xef.store.postgresql.connection
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.*
import javax.sql.DataSource

class ChatCompletionAssistant(
  private val dataSource: DataSource
) : Assistants {

  // Use postgres JSONB support to interop with the @Serializable types like request and response
  // and implement the assistant interface in terms of the SQL queries

  override suspend fun cancelRun(threadId: String, runId: String, configure: HttpRequestBuilder.() -> Unit): RunObject {
  }

  // use jsonb to store the request and response
  private val createAssistant = """
    INSERT INTO assistant (id, data, createdAt)
    VALUES (?, ?, ?)
  """.trimIndent()

  override suspend fun createAssistant(
    createAssistantRequest: CreateAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject =
    dataSource.connection {
      val id = UUID.randomUUID().toString()
      val assistantObject = assistantFromRequest(id, createAssistantRequest)
      update(createAssistant) {
        bind(id)
        bind(Json.encodeToString(AssistantObject.serializer(), assistantObject))
        bind(System.currentTimeMillis())
      }
      getAssistant(id, configure)
    }

  private fun assistantFromRequest(
    id: String,
    createAssistantRequest: CreateAssistantRequest
  ): AssistantObject = AssistantObject(
    id = id,
    `object` = AssistantObject.Object.assistant,
    createdAt = System.currentTimeMillis().toInt(),
    name = createAssistantRequest.name,
    description = createAssistantRequest.description,
    model = createAssistantRequest.model,
    instructions = createAssistantRequest.instructions,
    tools = createAssistantRequest.tools.orEmpty(),
    fileIds = createAssistantRequest.fileIds.orEmpty(),
    metadata = createAssistantRequest.metadata
  )


  override suspend fun createAssistantFile(
    assistantId: String,
    createAssistantFileRequest: CreateAssistantFileRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantFileObject =
    dataSource.connection {
      val id = UUID.randomUUID().toString()
      val assistantFileObject = assistantFileFromRequest(id, createAssistantFileRequest)
      update(
        """
        INSERT INTO assistant_file (id, assistantId, data)
        VALUES (?, ?, ?)
      """.trimIndent()
      ) {
        bind(id)
        bind(assistantId)
        bind(Json.encodeToString(AssistantFileObject.serializer(), assistantFileObject))
      }
      getAssistantFile(assistantId, id, configure)
    }

  override suspend fun createMessage(
    threadId: String,
    createMessageRequest: CreateMessageRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): MessageObject =
    dataSource.connection {
      val id = UUID.randomUUID().toString()
      val messageObject = messageObjectFromRequest(id, createMessageRequest, threadId)
      update(
        """
        INSERT INTO message (id, threadId, data)
        VALUES (?, ?, ?)
      """.trimIndent()
      ) {
        bind(id)
        bind(threadId)
        bind(Json.encodeToString(MessageObject.serializer(), messageObject))
      }
      getMessage(threadId, id, configure)
    }

  private fun messageObjectFromRequest(
    id: String,
    createMessageRequest: CreateMessageRequest,
    threadId: String
  ): MessageObject = MessageObject(
    id = id,
    `object` = MessageObject.Object.thread_message,
    createdAt = System.currentTimeMillis().toInt(),
    role = MessageObject.Role.valueOf(createMessageRequest.role.value),
    content = listOf(
      MessageObjectContentInner.CaseMessageContentTextObject(
        MessageContentTextObject(
          type = MessageContentTextObject.Type.text,
          text = MessageContentTextObjectText(
            value = createMessageRequest.content,
            annotations = emptyList()
          )
        )
      )
    ),
    fileIds = createMessageRequest.fileIds.orEmpty(),
    metadata = createMessageRequest.metadata,
    assistantId = null,
    runId = null,
    threadId = threadId
  )

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

  private val createThread = """
    INSERT INTO thread (id, data)
    VALUES (?, ?)
  """.trimIndent()

  override suspend fun createThread(
    createThreadRequest: CreateThreadRequest?,
    configure: HttpRequestBuilder.() -> Unit
  ): ThreadObject =
    dataSource.connection {
      val id = UUID.randomUUID().toString()
      val request = createThreadRequest ?: CreateThreadRequest()
      val threadObject = ThreadObject(
        id = id,
        `object` = ThreadObject.Object.thread,
        createdAt = System.currentTimeMillis().toInt(),
        metadata = request.metadata
      )
      update(createThread) {
        bind(id)
        bind(Json.encodeToString(ThreadObject.serializer(), threadObject))
      }
      createThreadRequest?.messages?.forEach { message ->
        createMessage(id, message, configure)
      }
      getThread(id, configure)
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

  private val deleteAssistant = """
    DELETE FROM assistant WHERE id = ?
  """.trimIndent()

  override suspend fun deleteAssistant(
    assistantId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantResponse =
    dataSource.connection {
      update(deleteAssistant) {
        bind(assistantId)
      }
      DeleteAssistantResponse(
        `object` = DeleteAssistantResponse.Object.assistant_deleted,
        id = assistantId,
        deleted = true
      )
    }

  override suspend fun deleteAssistantFile(
    assistantId: String,
    fileId: String,
    configure: HttpRequestBuilder.() -> Unit
  ): DeleteAssistantFileResponse {
    TODO("Not yet implemented")
  }

  private val deleteThread = """
    DELETE FROM thread WHERE id = ?
  """.trimIndent()

  override suspend fun deleteThread(threadId: String, configure: HttpRequestBuilder.() -> Unit): DeleteThreadResponse =
    dataSource.connection {
      update(deleteThread) {
        bind(threadId)
      }
      DeleteThreadResponse(
        `object` = DeleteThreadResponse.Object.thread_deleted,
        id = threadId,
        deleted = true
      )
    }

  private val getAssistant = """
    SELECT data FROM assistant WHERE id = ?
  """.trimIndent()

  override suspend fun getAssistant(assistantId: String, configure: HttpRequestBuilder.() -> Unit): AssistantObject =
    dataSource.connection {
      queryOneOrNull(getAssistant, { bind(assistantId) }) {
        Json.decodeFromString(AssistantObject.serializer(), string())
      } ?: throw IllegalStateException("Assistant '$assistantId' not found")
    }

  private fun fromPersistentTools(tool: AssistantTool): AssistantObjectToolsInner = when (tool) {
    AssistantTool.CodeInterpreter ->
      AssistantObjectToolsInner.CaseAssistantToolsCode(
        AssistantToolsCode(type = AssistantToolsCode.Type.code_interpreter)
      )

    is AssistantTool.Function -> AssistantObjectToolsInner.CaseAssistantToolsFunction(
      AssistantToolsFunction(
        type = AssistantToolsFunction.Type.function,
        function = FunctionObject(
          name = tool.name,
          parameters = Json.parseToJsonElement(tool.parameters) as? JsonObject
        )
      )
    )

    AssistantTool.Retrieval -> AssistantObjectToolsInner.CaseAssistantToolsRetrieval(
      AssistantToolsRetrieval(type = AssistantToolsRetrieval.Type.retrieval)
    )
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

  override suspend fun getRun(threadId: String, runId: String, configure: HttpRequestBuilder.() -> Unit): RunObject {
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

  private val getThread = """
    SELECT data FROM thread WHERE id = ?
  """.trimIndent()

  override suspend fun getThread(threadId: String, configure: HttpRequestBuilder.() -> Unit): ThreadObject =
    dataSource.connection {
      queryOneOrNull(getThread, { bind(threadId) }) {
        Json.decodeFromString(ThreadObject.serializer(), string())
      } ?: throw IllegalStateException("Thread '$threadId' not found")
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

  private val listAssistants =
    """
    SELECT data FROM assistant
    WHERE id > ?
    ORDER BY id
    LIMIT ?
    """.trimIndent()

  override suspend fun listAssistants(
    limit: Int?,
    order: Assistants.OrderListAssistants?,
    after: String?, // assistant id can be null
    before: String?, // assistant id can be null
    configure: HttpRequestBuilder.() -> Unit
  ): ListAssistantsResponse =
    //must also consider `before` in the query
    dataSource.connection {
      val assistants = queryAsList(
        sql = listAssistants
        , binders = {
          bind(after)
          bind(limit)
        }, mapper = {
          Json.decodeFromString(AssistantObject.serializer(), string())
        })
      ListAssistantsResponse(
        `object` = "list",
        data = assistants,
        firstId = assistants.firstOrNull()?.id,
        lastId = assistants.lastOrNull()?.id,
        hasMore = assistants.size == limit
      )
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

  private val listMessages =
    """
    SELECT data FROM message
    WHERE threadId = ?
    ORDER BY createdAt
    LIMIT ?
    """.trimIndent()

  override suspend fun listMessages(
    threadId: String,
    limit: Int?,
    order: Assistants.OrderListMessages?,
    after: String?,
    before: String?,
    configure: HttpRequestBuilder.() -> Unit
  ): ListMessagesResponse =
    dataSource.connection {
      val messages = queryAsList(
        sql = listMessages
        , binders = {
          bind(threadId)
          bind(limit)
        }, mapper = {
          Json.decodeFromString(MessageObject.serializer(), string())
        })
      ListMessagesResponse(
        `object` = "list",
        data = messages,
        firstId = messages.firstOrNull()?.id,
        lastId = messages.lastOrNull()?.id,
        hasMore = messages.size == limit
      )
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

  private val modifyAssistant = """
    UPDATE assistant SET data = ? WHERE id = ?
  """.trimIndent()

  override suspend fun modifyAssistant(
    assistantId: String,
    modifyAssistantRequest: ModifyAssistantRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): AssistantObject =
    dataSource.connection {
      val assistantObject = getAssistant(assistantId, configure)
      val modifiedAssistantObject = modifyAssistantFromRequest(assistantObject, modifyAssistantRequest)
      update(modifyAssistant) {
        bind(Json.encodeToString(AssistantObject.serializer(), modifiedAssistantObject))
        bind(assistantId)
      }
      getAssistant(assistantId, configure)
    }

  private fun modifyAssistantFromRequest(
    assistantObject: AssistantObject,
    modifyAssistantRequest: ModifyAssistantRequest
  ): AssistantObject = AssistantObject(
    name = modifyAssistantRequest.name,
    description = modifyAssistantRequest.description,
    model = modifyAssistantRequest.model ?: assistantObject.model,
    instructions = modifyAssistantRequest.instructions,
    tools = modifyAssistantRequest.tools.orEmpty(),
    fileIds = modifyAssistantRequest.fileIds.orEmpty(),
    metadata = modifyAssistantRequest.metadata,
    id = assistantObject.id,
    `object` = assistantObject.`object`,
    createdAt = assistantObject.createdAt
  )

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
