package com.xebia.functional.xef.server.ai.providers

import com.xebia.functional.xef.server.ai.copyFrom
import com.xebia.functional.xef.server.ai.providers.mlflow.*
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class MLflowApiProvider(
    private val gatewayUrl: String,
    private val pathProvider: PathProvider,
    private val client: HttpClient
): ApiProvider {

    private val mappedChatFields: Map<String, String> = mapOf("n" to "candidate_count")
    private val mappedEmbeddingsFields: Map<String, String> = mapOf("input" to "text")

    private val chatKeys = setOf("messages", "temperature", "candidate_count", "stop", "max_tokens")
    private val embeddingsKeys = setOf("text")

    private val json = Json { explicitNulls = false }

    private suspend inline fun <reified T, reified R> makeRequest(
        call: ApplicationCall,
        requestData: JsonObject,
        path: (String, UserIdPrincipal) -> String?,
        mappedFields: Map<String, String>,
        keys: Set<String>,
        map: (T) -> R
    ) {
        val model = requestData["model"]!!.jsonPrimitive.content
        val principal = call.principal<UserIdPrincipal>() ?: throw XefExceptions.AuthorizationException("")
        val resolvedPath = path(model, principal)
        resolvedPath ?. let {
            val newData = requestData.mapKeys { mappedFields.getOrDefault(it.key, it.key) }.filterKeys { keys.contains(it) }
            val newBody = json.encodeToString(JsonObject(newData)).toByteArray(Charsets.UTF_8)
            val response = client.post("$gatewayUrl$resolvedPath")  {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(newBody)
            }
            // We don't pass the `Content-Length`.
            // Ktor will add it when sending the Json back to the response
            call.response.headers.copyFrom(response.headers, "Content-Length")
            if (response.status.isSuccess()) {
                val stringResponseBody = response.bodyAsText()
                val responseData = Json.decodeFromString<T>(stringResponseBody)
                call.respond(response.status, Json.encodeToString(map(responseData)).toByteArray(Charsets.UTF_8))
            } else call.respond(response.status, response.readBytes())
        } ?: let {
            call.respond(HttpStatusCode.NotFound, "Model not found")
        }

    }


    override suspend fun chatRequest(call: ApplicationCall, requestBody: ByteArray) {
        val stringRequestBody = requestBody.toString(Charsets.UTF_8)
        val requestData = Json.decodeFromString<JsonObject>(stringRequestBody)
        val isStream = requestData["stream"]?.jsonPrimitive?.boolean ?: false
        if (isStream) {
            call.respond(HttpStatusCode.NotImplemented, "Stream not supported")
        } else {
            makeRequest<ChatResponse, OpenAIResponse>(
                call,
                requestData,
                { m, p -> pathProvider.chatPath(m, p) } ,
                mappedChatFields,
                chatKeys,
                { it.toOpenAI() }
            )
        }
    }

    override suspend fun embeddingsRequest(call: ApplicationCall, requestBody: ByteArray) {
        val stringRequestBody = requestBody.toString(Charsets.UTF_8)
        val requestData = Json.decodeFromString<JsonObject>(stringRequestBody)
        makeRequest<EmbeddingsResponse, OpenAIEmbeddingResponse>(
            call,
            requestData,
            { m, p -> pathProvider.embeddingsPath(m, p) } ,
            mappedEmbeddingsFields,
            embeddingsKeys,
            { it.toOpenAI() }
        )
    }
}

suspend fun mlflowApiProvider(gatewayUrl: String, client: HttpClient): MLflowApiProvider {
    val pathProvider = mlflowPathProvider(gatewayUrl, client)
    return MLflowApiProvider(gatewayUrl, pathProvider, client)
}