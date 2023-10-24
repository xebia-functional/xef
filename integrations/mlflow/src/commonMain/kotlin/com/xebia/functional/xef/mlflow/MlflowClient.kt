package com.xebia.functional.xef.mlflow

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class MlflowClient(
    private val gatewayUrl: String
) : AutoClose by autoClose() {
    private val http: HttpClient = jsonHttpClient()

    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun routes(): List<RouteDefinition> {
        val response = http.get("$gatewayUrl/api/2.0/gateway/routes/")
        if (response.status.isSuccess()) {
            val textResponse = response.bodyAsText()
            val data = json.decodeFromString<RoutesResponse>(textResponse)
            return data.routes
        } else {
            throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
        }
    }

    suspend fun searchRoutes(): List<RouteDefinition> = routes()

    suspend fun getRoute(name: String): RouteDefinition? = routes().find { it.name == name }

    suspend fun prompt(
        route: String,
        prompt: String,
        candidateCount: Int? = null,
        temperature: Double? = null,
        maxTokens: Int? = null,
        stop: List<String>? = null
    ): PromptResponse {
        val body = Prompt(prompt, temperature, candidateCount, stop, maxTokens)
        val response =
            http.post(
                "$gatewayUrl/gateway/$route/invocations"
            ) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(body)
            }

        return if (response.status.isSuccess())
            response.body<PromptResponse>()
        else if (response.status.value == 422)
            throw MLflowValidationError(response.status, response.body<ValidationError>().detail?.firstOrNull()?.msg ?: "Unknown error")
        else throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
    }

    suspend fun chat(
        route: String,
        messages: List<ChatMessage>,
        candidateCount: Int? = null,
        temperature: Double? = null,
        maxTokens: Int? = null,
        stop: List<String>? = null
    ): ChatResponse {
        val body = Chat(messages, temperature, candidateCount, stop, maxTokens)
        val response =
            http.post(
                "$gatewayUrl/gateway/$route/invocations"
            ) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(body)
            }

        return if (response.status.isSuccess())
            response.body<ChatResponse>()
        else if (response.status.value == 422)
            throw MLflowValidationError(response.status, response.body<ValidationError>().detail?.firstOrNull()?.msg ?: "Unknown error")
        else throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
    }

    suspend fun embeddings(route: String, text: List<String>): EmbeddingsResponse {
        val body = Embeddings(text)
        val response =
            http.post(
                "$gatewayUrl/gateway/$route/invocations"
            ) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(body)
            }

        return if (response.status.isSuccess())
            response.body<EmbeddingsResponse>()
        else if (response.status.value == 422)
            throw MLflowValidationError(response.status, response.body<ValidationError>().detail?.firstOrNull()?.msg ?: "Unknown error")
        else throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
    }

    class MLflowValidationError(httpStatusCode: HttpStatusCode, error: String) :
        IllegalStateException("$httpStatusCode: $error")

    class MLflowClientUnexpectedError(httpStatusCode: HttpStatusCode, error: String) :
        IllegalStateException("$httpStatusCode: $error")

}