package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.xebia.functional.gpt4all.toGpt4AllModel
import com.xebia.functional.xef.conversation.llm.openai.toOpenAIModel
import com.xebia.functional.xef.gcp.toGCPModel
import com.xebia.functional.xef.llm.LLM
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.EmptySerializersModule

internal class ChatConfig private constructor(
  val call: ApplicationCall,
  val token: String,
  val conversationId: String?,
  val openaiRequest: ChatCompletionRequest,
  val modelId: String,
  val provider: Provider,
  val stream: Boolean,
) {
  val model: LLM by lazy {
    when (provider) {
      Provider.OPENAI -> modelId.toOpenAIModel(token)
      Provider.GPT4ALL -> modelId.toGpt4AllModel()
      Provider.GCP -> {
        val location = call.request.headers["xef-gcp-location"] ?: error("xef-gcp-location header is required")
        val projectId =
          call.request.headers["xef-gcp-project-id"] ?: error("xef-gcp-project-id header is required")
        modelId.toGCPModel(location, projectId)
      }
    }
  }

  companion object {

    internal fun String.provider(): Provider = when (this.substringBefore(":")) {
      "openai" -> Provider.OPENAI
      "gpt4all" -> Provider.GPT4ALL
      "gcp" -> Provider.GCP
      else -> Provider.OPENAI
    }

    internal suspend operator fun invoke(call : ApplicationCall): ChatConfig {
      val token = call.getToken()
      val body = call.receive<String>()
      val stream = Json.decodeFromString<JsonObject>(body)["stream"]?.jsonPrimitive?.boolean ?: false
      val conversationId = call.getConversationId()
      val openaiRequest = Json { ignoreUnknownKeys = true }.decodeFromString<ChatCompletionRequest>(body)
      val modelId = openaiRequest.model.id
      val provider = modelId.provider()
      return ChatConfig(call, token, conversationId, openaiRequest, modelId, provider, stream)
    }
  }
}
