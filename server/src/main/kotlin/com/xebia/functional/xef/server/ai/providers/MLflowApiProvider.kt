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

    private val json = Json { explicitNulls = false }

    private suspend inline fun <reified R1, reified R2> makeRequest(
        call: ApplicationCall,
        requestData: JsonObject,
        reqResMap: ReqResMap<R1, R2>
    ) {
        val model = requestData["model"]!!.jsonPrimitive.content
        val principal = call.principal<UserIdPrincipal>() ?: throw XefExceptions.AuthorizationException("")
        val resolvedPath = reqResMap.path(pathProvider, model, principal)
        resolvedPath ?. let {
            val newData = reqResMap.mapRequest(requestData)
            val newBody = json.encodeToString(newData).toByteArray(Charsets.UTF_8)
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
                val responseData = Json.decodeFromString<R1>(stringResponseBody)
                call.respond(response.status, Json.encodeToString(reqResMap.mapResponse(responseData)).toByteArray(Charsets.UTF_8))
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
            makeRequest<ChatResponse, OpenAIResponse>(call, requestData, ChatReqResMap)
        }
    }

    override suspend fun embeddingsRequest(call: ApplicationCall, requestBody: ByteArray) {
        val stringRequestBody = requestBody.toString(Charsets.UTF_8)
        val requestData = Json.decodeFromString<JsonObject>(stringRequestBody)
        makeRequest<EmbeddingsResponse, OpenAIEmbeddingResponse>(call, requestData, EmbeddingsReqResMap)
    }
}

suspend fun mlflowApiProvider(gatewayUrl: String, client: HttpClient): MLflowApiProvider {
    val pathProvider = mlflowPathProvider(gatewayUrl, client)
    return MLflowApiProvider(gatewayUrl, pathProvider, client)
}