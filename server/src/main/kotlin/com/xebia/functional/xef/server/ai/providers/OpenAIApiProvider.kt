package com.xebia.functional.xef.server.ai.providers

import io.ktor.client.*
import io.ktor.server.application.*
import kotlinx.serialization.json.*
import com.xebia.functional.xef.server.ai.*

class OpenAIApiProvider(private val client: HttpClient): ApiProvider {

    private val openAiUrl = "https://api.openai.com/v1"

    override suspend fun chatRequest(call: ApplicationCall, requestBody: ByteArray) {
        val stringBody = requestBody.toString(Charsets.UTF_8)
        val data = Json.decodeFromString<JsonObject>(stringBody)
        val isStream = data["stream"]?.jsonPrimitive?.boolean ?: false
        if (!isStream) {
            client.makeRequest(call, "$openAiUrl/chat/completions", requestBody)
        } else {
            client.makeStreaming(call, "$openAiUrl/chat/completions", requestBody)
        }
    }

    override suspend fun embeddingsRequest(call: ApplicationCall, requestBody: ByteArray) {
        client.makeRequest(call, "$openAiUrl/embeddings", requestBody)
    }
}