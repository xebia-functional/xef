package com.xebia.functional.xef.llm.openai

import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

interface OpenAIClient {
  suspend fun createCompletion(request: CompletionRequest): CompletionResult

  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

  suspend fun createChatCompletionWithFunctions(request: ChatCompletionRequestWithFunctions): ChatCompletionResponseWithFunctions

  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult

  suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse
}

@OptIn(ExperimentalStdlibApi::class)
class KtorOpenAIClient(private val config: OpenAIConfig) : OpenAIClient, AutoCloseable {

  private val httpClient: HttpClient = HttpClient {
    install(HttpTimeout)
    install(ContentNegotiation) { json() }
    defaultRequest { url(config.baseUrl) }
  }

  private val logger: KLogger = KotlinLogging.logger {}

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response =
      httpClient.post {
        url { path("completions") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeoutMillis }
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
        timeout { requestTimeoutMillis = config.requestTimeoutMillis }
      }

    val body: ChatCompletionResponse = response.bodyOrError()
    with(body.usage) {
      logger.debug {
        "Chat Completion Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createChatCompletionWithFunctions(request: ChatCompletionRequestWithFunctions): ChatCompletionResponseWithFunctions {
    val response =
      httpClient.post {
        url { path("chat/completions") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeoutMillis }
      }

    val body: ChatCompletionResponseWithFunctions = response.bodyOrError()
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
        timeout { requestTimeoutMillis = config.requestTimeoutMillis }
      }
      .bodyOrError()

  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse =
    httpClient
      .post {
        url { path("images/generations") }
        configure(config.token, request)
        timeout { requestTimeoutMillis = config.requestTimeoutMillis }
      }
      .bodyOrError()

  override fun close() = httpClient.close()
}

private suspend inline fun <reified T> HttpResponse.bodyOrError(): T =
  if (status == HttpStatusCode.OK) body() else throw OpenAIClientException(status, body())

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
