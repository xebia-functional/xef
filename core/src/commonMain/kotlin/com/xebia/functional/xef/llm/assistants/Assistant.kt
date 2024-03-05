package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.apis.AssistantApi
import com.xebia.functional.openai.apis.AssistantsApi
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.models.AssistantObject
import com.xebia.functional.openai.models.CreateAssistantRequest
import com.xebia.functional.openai.models.ModifyAssistantRequest
import com.xebia.functional.openai.models.ext.assistant.AssistantTools
import com.xebia.functional.xef.llm.fromEnvironment
import io.ktor.util.logging.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class Assistant(
  val assistantId: String,
  val toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
  private val assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
  private val api: AssistantApi = fromEnvironment(::AssistantApi)
) {

  constructor(
    assistantObject: AssistantObject,
    toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
    assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
    api: AssistantApi = fromEnvironment(::AssistantApi)
  ) : this(assistantObject.id, toolsConfig, assistantsApi, api)

  suspend fun get(): AssistantObject = assistantsApi.getAssistant(assistantId).body()

  suspend fun modify(modifyAssistantRequest: ModifyAssistantRequest): Assistant =
    Assistant(
      api.modifyAssistant(assistantId, modifyAssistantRequest).body(),
      toolsConfig,
      assistantsApi,
      api
    )

  suspend inline fun getToolRegistered(name: String, args: String): JsonElement =
    try {
      val toolConfig = toolsConfig.firstOrNull { it.functionObject.name == name }

      val toolSerializer = toolConfig?.serializers ?: error("Function $name not registered")
      val input = ApiClient.JSON_DEFAULT.decodeFromString(toolSerializer.inputSerializer, args)

      val tool: Tool<Any?, Any?> = toolConfig.tool as Tool<Any?, Any?>

      val output: Any? = tool(input)
      ApiClient.JSON_DEFAULT.encodeToJsonElement(
        toolSerializer.outputSerializer as KSerializer<Any?>,
        output
      )
    } catch (e: Exception) {
      val message = "Error calling to tool registered $name: ${e.message}"
      val logger = KtorSimpleLogger("Functions")
      logger.error(message, e)
      JsonObject(mapOf("error" to JsonPrimitive(message)))
    }

  companion object {

    suspend operator fun invoke(
      model: String,
      name: String? = null,
      description: String? = null,
      instructions: String? = null,
      tools: List<AssistantTools> = arrayListOf(),
      fileIds: List<String> = arrayListOf(),
      metadata: JsonObject? = null,
      toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
      assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
      api: AssistantApi = fromEnvironment(::AssistantApi)
    ): Assistant =
      Assistant(
        CreateAssistantRequest(
          model = model,
          name = name,
          description = description,
          instructions = instructions,
          tools = tools,
          fileIds = fileIds,
          metadata = metadata
        ),
        toolsConfig,
        assistantsApi,
        api
      )

    suspend operator fun invoke(
      request: CreateAssistantRequest,
      toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
      assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
      api: AssistantApi = fromEnvironment(::AssistantApi)
    ): Assistant {
      val response = assistantsApi.createAssistant(request)
      return Assistant(response.body(), toolsConfig, assistantsApi, api)
    }
  }
}
