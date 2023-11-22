package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.apis.AssistantApi
import com.xebia.functional.openai.apis.AssistantsApi
import com.xebia.functional.openai.models.AssistantObject
import com.xebia.functional.openai.models.AssistantObjectToolsInner
import com.xebia.functional.openai.models.CreateAssistantRequest
import com.xebia.functional.openai.models.ModifyAssistantRequest
import com.xebia.functional.xef.llm.fromEnvironment
import io.ktor.client.statement.*

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

  //  suspend fun createFile(
  //    fileId: String
  //  ): File =
  //    File(
  //      assistantsApi.createAssistantFile(assistantId,
  // CreateAssistantFileRequest(fileId)).body().id,
  //      assistantsApi,
  //      api
  //    )

  suspend fun modify(modifyAssistantRequest: ModifyAssistantRequest): Assistant =
    Assistant(api.modifyAssistant(assistantId, modifyAssistantRequest).body(), assistantsApi, api)

  companion object {

    suspend operator fun invoke(
      model: String,
      name: String? = null,
      description: String? = null,
      instructions: String? = null,
      tools: List<AssistantObjectToolsInner> = arrayListOf(),
      fileIds: List<String> = arrayListOf(),
      metadata: String? = null,
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
      println(response.response.bodyAsText())
      return Assistant(response.body(), assistantsApi, api)
    }
  }
}
