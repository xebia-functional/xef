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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MlflowClient(private val gatewayUrl: String) : AutoClose by autoClose() {
  private val http: HttpClient = jsonHttpClient()

  @Serializable
    data class RoutesResponse(
        val routes: List<RouteDefinition>
    )

    @Serializable
    data class RouteDefinition(
        val name: String,
        @SerialName("route_type")
        val routeType: String,
        val model: RouteModel,
        @SerialName("route_url")
        val routeUrl: String,
    )

    @Serializable
    data class RouteModel(
        val name: String,
        val provider: String,
    )

    @Serializable
  private data class Prompt(
    val prompt: String,
    val temperature: Double? = null,
    @SerialName("candidate_count") val candidateCount: Int? = null,
    val stop: List<String>? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null
  )

  @Serializable
  data class CandidateMetadata(@SerialName("finish_reason") val finishReason: String?)

  @Serializable data class PromptCandidate(val text: String, val metadata: CandidateMetadata?)

  @Serializable
  enum class RouteType {
    @SerialName("llm/v1/completions") COMPLETIONS,
    @SerialName("llm/v1/chat") CHAT,
    @SerialName("llm/v1/embeddings") EMBEDDINGS
  }

  @Serializable
  data class ResponseMetadata(
    val model: String,
    @SerialName("route_type") val routeType: RouteType,
    @SerialName("input_tokens") val inputTokens: Int? = null,
    @SerialName("output_tokens") val outputTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null
  )

  @Serializable
  data class PromptResponse(val candidates: List<PromptCandidate>, val metadata: ResponseMetadata)

  @Serializable data class ValidationDetail(val msg: String, val type: String)

  @Serializable data class ValidationError(val detail: List<ValidationDetail>?)

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
                "$gatewayUrl/gateway/$route/invocations") {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        setBody(body)
      }

    return if (response.status.isSuccess()) response.body<PromptResponse>()
    else if (response.status.value == 422)
      throw MLflowValidationError(
        response.status,
        response.body<ValidationError>().detail?.firstOrNull()?.msg ?: "Unknown error"
      )
    else throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
  }

  @Serializable
  enum class ChatRole {
    @SerialName("system") SYSTEM,
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT
  }

  @Serializable data class ChatMessage(val role: ChatRole, val content: String)

  @Serializable
  private data class Chat(
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    @SerialName("candidate_count") val candidateCount: Int? = null,
    val stop: List<String>? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null
  )

  @Serializable data class ChatCandidate(val message: ChatMessage, val metadata: CandidateMetadata)

  @Serializable
  data class ChatResponse(val candidates: List<ChatCandidate>, val metadata: ResponseMetadata)

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
      http.post("$gatewayUrl/gateway/$route/invocations") {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        setBody(body)
      }

    return if (response.status.isSuccess()) response.body<ChatResponse>()
    else if (response.status.value == 422)
      throw MLflowValidationError(
        response.status,
        response.body<ValidationError>().detail?.firstOrNull()?.msg ?: "Unknown error"
      )
    else throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
  }

  @Serializable private data class Embeddings(val text: List<String>)

  @Serializable
  data class EmbeddingsResponse(val embeddings: List<List<Float>>, val metadata: ResponseMetadata)

  suspend fun embeddings(route: String, text: List<String>): EmbeddingsResponse {
    val body = Embeddings(text)
    val response =
      http.post("$gatewayUrl/gateway/$route/invocations") {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        setBody(body)
      }

    return if (response.status.isSuccess()) response.body<EmbeddingsResponse>()
    else if (response.status.value == 422)
      throw MLflowValidationError(
        response.status,
        response.body<ValidationError>().detail?.firstOrNull()?.msg ?: "Unknown error"
      )
    else throw MLflowClientUnexpectedError(response.status, response.bodyAsText())
  }

  class MLflowValidationError(val httpStatusCode: HttpStatusCode, val error: String) :
    IllegalStateException("$httpStatusCode: $error")

  class MLflowClientUnexpectedError(val httpStatusCode: HttpStatusCode, val error: String) :
    IllegalStateException("$httpStatusCode: $error")
}
