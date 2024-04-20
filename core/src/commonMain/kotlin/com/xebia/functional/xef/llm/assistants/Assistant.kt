package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.generated.api.Assistants
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.AssistantThread.Companion.defaultConfig
import com.xebia.functional.xef.llm.models.functions.buildJsonSchema
import io.ktor.util.logging.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlMap
import net.mamoe.yamlkt.literalContentOrNull
import net.mamoe.yamlkt.toYamlElement

class Assistant(
  val assistantId: String,
  val toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
  val config: Config = Config(),
  private val assistantsApi: Assistants = OpenAI(config, logRequests = true).assistants,
) {

  constructor(
    assistantObject: AssistantObject,
    toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
    config: Config = Config(),
    assistantsApi: Assistants = OpenAI(config, logRequests = true).assistants,
  ) : this(assistantObject.id, toolsConfig, config, assistantsApi)

  suspend fun get(): AssistantObject =
    assistantsApi.getAssistant(assistantId, configure = ::defaultConfig)

  suspend fun modify(modifyAssistantRequest: ModifyAssistantRequest): Assistant =
    Assistant(
      assistantsApi.modifyAssistant(
        assistantId,
        modifyAssistantRequest,
        configure = ::defaultConfig
      ),
      toolsConfig,
      config,
      assistantsApi
    )

  suspend inline fun getToolRegistered(name: String, args: String): ToolOutput =
    try {
      val toolConfig = toolsConfig.firstOrNull { it.functionObject.name == name }

      val toolSerializer = toolConfig?.serializers ?: error("Function $name not registered")
      val input = toolSerializer.inputSerializer.deserialize(args)

      val tool: Tool<Any?, Any?> = toolConfig.tool as Tool<Any?, Any?>

      val schema = buildJsonSchema(toolSerializer.outputSerializer)
      val output: Any? = tool(input)
      val result =
        Json.encodeToJsonElement(toolSerializer.outputSerializer as KSerializer<Any?>, output)
      ToolOutput(schema, result)
    } catch (e: Exception) {
      val message = "Error calling to tool registered $name: ${e.message}"
      val logger = KtorSimpleLogger("Functions")
      logger.error(message, e)
      val result = JsonObject(mapOf("error" to JsonPrimitive(message)))
      ToolOutput(JsonObject(emptyMap()), result)
    }

  companion object {

    @Serializable data class ToolOutput(val schema: JsonObject, val result: JsonElement)

    suspend operator fun invoke(
      model: String,
      name: String? = null,
      description: String? = null,
      instructions: String? = null,
      tools: List<AssistantObjectToolsInner> = arrayListOf(),
      fileIds: List<String> = arrayListOf(),
      metadata: JsonObject? = null,
      toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
      config: Config = Config(),
      assistantsApi: Assistants = OpenAI(config, logRequests = true).assistants,
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
        config,
        assistantsApi
      )

    suspend operator fun invoke(
      request: CreateAssistantRequest,
      toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
      config: Config = Config(),
      assistantsApi: Assistants = OpenAI(config, logRequests = true).assistants,
    ): Assistant {
      val response = assistantsApi.createAssistant(request, configure = ::defaultConfig)
      return Assistant(response, toolsConfig, config, assistantsApi)
    }

    suspend fun fromConfig(
      request: String,
      toolsConfig: List<Tool.Companion.ToolConfig<*, *>> = emptyList(),
      config: Config = Config(),
      assistantsApi: Assistants = OpenAI(config, logRequests = true).assistants,
    ): Assistant {
      val parsed = Yaml.Default.decodeYamlMapFromString(request)
      val assistantRequest =
        AssistantRequest(
          assistantId = parsed["assistant_id"]?.literalContentOrNull,
          model = parsed["model"]?.literalContentOrNull ?: error("model is required"),
          name = parsed["name"]?.literalContentOrNull,
          description = parsed["description"]?.literalContentOrNull,
          instructions =
            parsed["instructions"]?.literalContentOrNull
              ?: (parsed["instructions"] as? YamlMap)?.let {
                Yaml.encodeToString(YamlMap.serializer(), it)
              },
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
                        val className =
                          element["name".toYamlElement()]?.toString()
                            ?: error("simple `name` for `function` is required")
                        val foundConfig =
                          toolsConfig.firstOrNull { it.tool::class.simpleName == className }
                        if (foundConfig != null) {
                          val functionObject = foundConfig.functionObject
                          AssistantTool.Function(
                            functionObject.name,
                            functionObject.description ?: "",
                            functionObject.parameters?.let { el ->
                              Json.encodeToString(JsonObject.serializer(), el)
                            } ?: ""
                          )
                        } else {
                          error("Tool $className not found in toolsConfig")
                        }
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
        val assistant =
          Assistant(
            assistantId = assistantRequest.assistantId,
            toolsConfig = toolsConfig,
            config = config,
            assistantsApi = assistantsApi,
          )

        assistant.modify(
          ModifyAssistantRequest(
            model = assistantRequest.model,
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
          request =
            CreateAssistantRequest(
              model = assistantRequest.model,
              name = assistantRequest.name,
              description = assistantRequest.description,
              instructions = assistantRequest.instructions,
              tools = assistantTools(assistantRequest),
              fileIds = assistantRequest.fileIds,
              metadata = null // assistantRequest.metadata
            ),
          toolsConfig = toolsConfig,
          config = config,
          assistantsApi = assistantsApi,
        )
    }

    private fun assistantTools(
      assistantRequest: AssistantRequest
    ): List<AssistantObjectToolsInner> =
      assistantRequest.tools.orEmpty().map {
        when (it) {
          is AssistantTool.CodeInterpreter ->
            AssistantObjectToolsInner.CaseAssistantToolsCode(
              AssistantToolsCode(type = AssistantToolsCode.Type.code_interpreter)
            )
          is AssistantTool.Retrieval ->
            AssistantObjectToolsInner.CaseAssistantToolsRetrieval(
              AssistantToolsRetrieval(type = AssistantToolsRetrieval.Type.retrieval)
            )
          is AssistantTool.Function ->
            AssistantObjectToolsInner.CaseAssistantToolsFunction(
              AssistantToolsFunction(
                type = AssistantToolsFunction.Type.function,
                function =
                  FunctionObject(
                    name = it.name,
                    parameters =
                      Json.parseToJsonElement(it.parameters) as? JsonObject
                        ?: JsonObject(emptyMap()),
                    description = it.description
                  )
              )
            )
        }
      }
  }
}
