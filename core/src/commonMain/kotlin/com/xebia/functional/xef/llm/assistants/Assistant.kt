package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.apis.AssistantApi
import com.xebia.functional.openai.apis.AssistantsApi
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.models.AssistantObject
import com.xebia.functional.openai.models.CreateAssistantRequest
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ModifyAssistantRequest
import com.xebia.functional.openai.models.ext.assistant.AssistantTools
import com.xebia.functional.openai.models.ext.assistant.AssistantToolsCode
import com.xebia.functional.openai.models.ext.assistant.AssistantToolsFunction
import com.xebia.functional.openai.models.ext.assistant.AssistantToolsRetrieval
import com.xebia.functional.xef.llm.fromEnvironment
import io.ktor.client.statement.*
import io.ktor.util.logging.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.literalContentOrNull
import net.mamoe.yamlkt.toYamlElement

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

    suspend fun fromConfig(
      request: String,
      assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
      api: AssistantApi = fromEnvironment(::AssistantApi)
    ): Assistant {
      val parsed = Yaml.Default.decodeYamlMapFromString(request)
      val assistantRequest =
        AssistantRequest(
          assistantId = parsed["assistant_id"]?.literalContentOrNull,
          model = parsed["model"]?.literalContentOrNull ?: error("model is required"),
          name = parsed["name"]?.literalContentOrNull,
          description = parsed["description"]?.literalContentOrNull,
          instructions = parsed["instructions"]?.literalContentOrNull,
          tools =
            parsed["tools"]?.let { list ->
              (list as List<*>).map { element ->
                when (element) {
                  is Map<*, *> -> {
                    val tool =
                      element["type".toYamlElement()]?.toString() ?: error("type is required")
                    when (tool) {
                      "code_interpreter" -> AssistantTool.CodeInterpreter
                      "retrieval" -> AssistantTool.Retrieval
                      "function" -> {
                        val name =
                          element["name".toYamlElement()]?.toString() ?: error("name is required")
                        val description =
                          element["description".toYamlElement()]?.toString()
                            ?: error("description is required")
                        val parameters =
                          element["parameters".toYamlElement()]?.toString()
                            ?: error("parameters is required")
                        AssistantTool.Function(name, description, parameters)
                      }
                      else -> error("unknown tool $tool")
                    }
                  }
                  else -> error("unknown tool $element")
                }
              }
            },
          fileIds =
            parsed["file_ids"]?.let { (it as List<*>).map { it.toString() } } ?: emptyList(),
        )
      return if (assistantRequest.assistantId != null) {
        val assistant = Assistant(assistantId = assistantRequest.assistantId, assistantsApi = assistantsApi, api = api)
        assistant.modify(
          ModifyAssistantRequest(
            name = assistantRequest.name,
            description = assistantRequest.description,
            instructions = assistantRequest.instructions,
            tools = assistantTools(assistantRequest),
            fileIds = assistantRequest.fileIds,
            metadata = null // assistantRequest.metadata
          )
        )
      } else
        Assistant(
          CreateAssistantRequest(
            model = assistantRequest.model,
            name = assistantRequest.name,
            description = assistantRequest.description,
            instructions = assistantRequest.instructions,
            tools = assistantTools(assistantRequest),
            fileIds = assistantRequest.fileIds,
            metadata = null // assistantRequest.metadata
          ),
          assistantsApi,
          api
        )
    }

    private fun assistantTools(assistantRequest: AssistantRequest) =
      assistantRequest.tools?.map {
        when (it) {
          is AssistantTool.CodeInterpreter -> AssistantToolsCode()
          is AssistantTool.Retrieval -> AssistantToolsRetrieval()
          is AssistantTool.Function ->
            AssistantToolsFunction(
              function =
                FunctionObject(
                  name = it.name,
                  parameters =
                    ApiClient.JSON_DEFAULT.parseToJsonElement(it.parameters) as? JsonObject
                      ?: JsonObject(emptyMap()),
                  description = it.description
                )
            )
        }
      }
  }
}
