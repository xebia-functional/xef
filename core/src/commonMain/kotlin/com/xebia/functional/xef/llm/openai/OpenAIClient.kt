package com.xebia.functional.xef.llm.openai

import arrow.resilience.retryOrElse
import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.OpenAIConfig
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.*
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

@OptIn(ExperimentalStdlibApi::class)
class KtorOpenAIClient(private val config: OpenAIConfig) : OpenAIClient, AutoCloseable {

  private val httpClient: HttpClient = HttpClient {
    install(HttpTimeout)
    install(ContentNegotiation) { json() }
    defaultRequest { url(config.baseUrl.encodedPath) }
  }

  private val logger: KLogger = KotlinLogging.logger {}

  suspend fun retry(block: suspend () -> HttpResponse): HttpResponse =
    config.retryConfig
      .schedule()
      .log { error, retriesSoFar ->
        logger.error(error) { "Open AI call failed. So far we have retried $retriesSoFar times." }
      }
      .retryOrElse({ block() }) { error, retries ->
        logger.error(error) { "Open AI call failed. Giving up after $retries retries" }
        throw error
      }

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response = retry {
      httpClient.post {
        url { path("completions") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }
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
    val response = retry {
      httpClient.post {
        url { path("chat/completions") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }
    }
    val body: ChatCompletionResponse = response.bodyOrError()
    with(body.usage) {
      logger.debug {
        "Chat Completion Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response = retry {
      httpClient.post {
        url { path("embeddings") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }
    }
    return response.bodyOrError()
  }

  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse {
    val response = retry {
      httpClient.post {
        url { path("images/generations") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
      }
    }
    return response.bodyOrError()
  }

  override fun close() = httpClient.close()
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
