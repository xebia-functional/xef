package com.xebia.functional.xef.server.ai.providers

import com.xebia.functional.xef.server.ai.*
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.*

class OpenAIApiProvider(private val client: HttpClient, private val baseUrl: String) : ApiProvider {

  override suspend fun PipelineContext<Unit, ApplicationCall>.chatRequest() {
    val requestBody = call.receiveChannel().toByteArray()
    val stringBody = requestBody.toString(Charsets.UTF_8)
    val data = Json.decodeFromString<JsonObject>(stringBody)
    val isStream = data["stream"]?.jsonPrimitive?.boolean ?: false
    if (!isStream) {
      client.makeRequest(call, "$baseUrl/chat/completions", requestBody)
    } else {
      client.makeStreaming(call, "$baseUrl/chat/completions", requestBody)
    }
  }

  override suspend fun PipelineContext<Unit, ApplicationCall>.embeddingsRequest() {
    val requestBody = call.receiveChannel().toByteArray()
    client.makeRequest(call, "$baseUrl/embeddings", requestBody)
  }
}
