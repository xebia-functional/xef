package com.xebia.functional.xef.server.ai.providers

import com.xebia.functional.xef.server.ai.copyFrom
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import com.xebia.functional.xef.server.models.mlflow.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class MLflowApiProvider(
    private val gatewayUrl: String,
    private val pathProvider: PathProvider,
    private val client: HttpClient
): ApiProvider {

    override suspend fun chatRequest(call: ApplicationCall, requestBody: ByteArray) {
        val stringRequestBody = requestBody.toString(Charsets.UTF_8)
        val requestData = Json.decodeFromString<JsonObject>(stringRequestBody)
        val isStream = requestData["stream"]?.jsonPrimitive?.boolean ?: false
        if (isStream) {
            call.respond(HttpStatusCode.NotImplemented, "Stream not supported")
        } else {
            val model = requestData["model"]!!.jsonPrimitive.content
            val principal = call.principal<UserIdPrincipal>() ?: throw XefExceptions.AuthorizationException("")
            val path = pathProvider.chatPath(model, principal)
            val newData = requestData.filterKeys { !filterOutFields.contains(it) }.mapKeys {mappedFields.getOrDefault(it.key, it.key) }
            val newBody = Json.encodeToString(JsonObject(newData)).toByteArray(Charsets.UTF_8)
            val response = client.post("$gatewayUrl/$path/invocations")  {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(newBody)
            }
            // We don't pass the `Content-Length`.
            // Ktor will add it when sending the Json back to the response
            call.response.headers.copyFrom(response.headers, "Content-Length")
            if (response.status.isSuccess()) {
                val stringResponseBody = response.bodyAsText()
                val responseData = Json.decodeFromString<ChatResponse>(stringResponseBody)
                call.respond(response.status, Json.encodeToString(responseData.toOpenAI()).toByteArray(Charsets.UTF_8))
            } else call.respond(response.status, response.readBytes())
        }
    }

    override suspend fun embeddingsRequest(call: ApplicationCall, requestBody: ByteArray) {
        val stringRequestBody = requestBody.toString(Charsets.UTF_8)
        val requestData = Json.decodeFromString<JsonObject>(stringRequestBody)
        val model = requestData["model"]!!.jsonPrimitive.content
        val principal = call.principal<UserIdPrincipal>() ?: throw XefExceptions.AuthorizationException("")
        val path = pathProvider.embeddingsPath(model, principal)
        val newData = requestData.filterKeys { !it.equals("model", true) }
        val newBody = Json.encodeToString(JsonObject(newData)).toByteArray(Charsets.UTF_8)
        val response = client.post("$gatewayUrl/$path/invocations")  {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(newBody)
        }
        // We don't pass the `Content-Length`.
        // Ktor will add it when sending the Json back to the response
        call.response.headers.copyFrom(response.headers, "Content-Length")
        if (response.status.isSuccess()) {
            val stringResponseBody = response.bodyAsText()
            val responseData = Json.decodeFromString<EmbeddingsResponse>(stringResponseBody)
            // TODO - Any way to ignore nulls on serialization?
            call.respond(response.status, Json.encodeToString(responseData.toOpenAI()).toByteArray(Charsets.UTF_8))
        } else call.respond(response.status, response.readBytes())
    }
}