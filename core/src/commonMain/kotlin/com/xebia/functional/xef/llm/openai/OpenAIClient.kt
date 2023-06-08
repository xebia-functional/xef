package com.xebia.functional.xef.llm.openai

import arrow.fx.coroutines.ResourceScope
import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.httpClient
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface OpenAIClient {
  suspend fun createCompletion(request: CompletionRequest): CompletionResult
  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse
  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult
  suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse
}

@Serializable
data class ImagesGenerationRequest(
  val prompt: String,
  @SerialName("n") val numberImages: Int = 1,
  val size: String = "1024x1024",
  @SerialName("response_format") val responseFormat: String = "url",
  val user: String? = null
)

@Serializable
data class ImagesGenerationResponse(val created: Long, val data: List<ImageGenerationUrl>)

@Serializable data class ImageGenerationUrl(val url: String)

suspend fun ResourceScope.KtorOpenAIClient(config: OpenAIConfig): OpenAIClient =
  KtorOpenAIClient(httpClient(config.baseUrl), config)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val config: OpenAIConfig
) : OpenAIClient {

  private val logger: KLogger = KotlinLogging.logger {}

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response =
      httpClient.post {
        url { path("completions") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }

    val body: CompletionResult = response.bodyOrError()
    with(body.usage) {
      logger.debug {
        "Completion Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val response =
      httpClient.post {
        url { path("chat/completions") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }

    val body: ChatCompletionResponse = response.bodyOrError()
    with(body.usage) {
      logger.debug {
        "Chat Completion Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult =
    httpClient
      .post {
        url { path("embeddings") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }
      .bodyOrError()

  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse =
    httpClient
      .post {
        url { path("images/generations") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }
      .bodyOrError()
}

private suspend inline fun <reified T> HttpResponse.bodyOrError(): T =
  if (status == HttpStatusCode.OK) Json.decodeFromString(bodyAsText())
  else throw OpenAIClientException(status, Json.decodeFromString(bodyAsText()))

class OpenAIClientException(val httpStatusCode: HttpStatusCode, val error: Error) :
  IllegalStateException(
    """
  
  OpenAI error: ${error.error.type}
    message: ${error.error.message}
    StatusCode: $httpStatusCode 
    param: ${error.error.param}
    code: ${error.error.code}
      
      """
      .trimIndent()
  )

@Serializable
data class Error(val error: Description) {
  @Serializable
  data class Description(
    val message: String,
    val type: String,
    val param: String? = null,
    val code: String? = null
  )
}
