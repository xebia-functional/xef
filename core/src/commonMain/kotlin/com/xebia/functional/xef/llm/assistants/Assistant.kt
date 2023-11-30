package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.apis.AssistantApi
import com.xebia.functional.openai.apis.AssistantsApi
import com.xebia.functional.openai.models.AssistantObject
import com.xebia.functional.openai.models.CreateAssistantRequest
import com.xebia.functional.openai.models.ModifyAssistantRequest
import com.xebia.functional.openai.models.ext.assistant.AssistantTools
import com.xebia.functional.xef.llm.fromEnvironment
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject

class Assistant(
  val assistantId: String,
  private val assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
  private val api: AssistantApi = fromEnvironment(::AssistantApi)
) {

  constructor(
    assistantObject: AssistantObject,
    assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
    api: AssistantApi = fromEnvironment(::AssistantApi)
  ) : this(assistantObject.id, assistantsApi, api)

  suspend fun get(): AssistantObject = assistantsApi.getAssistant(assistantId).body()

  suspend fun modify(modifyAssistantRequest: ModifyAssistantRequest): Assistant =
    Assistant(api.modifyAssistant(assistantId, modifyAssistantRequest).body(), assistantsApi, api)

  companion object {

    suspend operator fun invoke(
      model: String,
      name: String? = null,
      description: String? = null,
      instructions: String? = null,
      tools: List<AssistantTools> = arrayListOf(),
      fileIds: List<String> = arrayListOf(),
      metadata: JsonObject? = null,
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
        assistantsApi,
        api
      )

    suspend operator fun invoke(
      request: CreateAssistantRequest,
      assistantsApi: AssistantsApi = fromEnvironment(::AssistantsApi),
      api: AssistantApi = fromEnvironment(::AssistantApi)
    ): Assistant {
      val response = assistantsApi.createAssistant(request)
      return Assistant(response.body(), assistantsApi, api)
    }
  }
}
