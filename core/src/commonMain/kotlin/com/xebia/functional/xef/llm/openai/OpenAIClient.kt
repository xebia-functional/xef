package com.xebia.functional.xef.llm.openai

import arrow.fx.coroutines.ResourceScope
import arrow.resilience.retry
import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.httpClient
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val logger: KLogger = KotlinLogging.logger {}

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

suspend fun ResourceScope.KtorOpenAIClient(
  config: OpenAIConfig,
  engine: HttpClientEngine? = null
): OpenAIClient = KtorOpenAIClient(httpClient(engine, config.baseUrl), config)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val config: OpenAIConfig
) : OpenAIClient {

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: HttpResponse =
      config.retryConfig.schedule().retry {
        httpClient.post {
          url { path("completions") }
          configure(config.token, request)
          timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
        }
      }
    val body: CompletionResult = response.body()
    with(body.usage) {
      logger.debug {
        "OpenAI Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val response: HttpResponse =
      config.retryConfig
        .schedule()
        .log { error, attempts ->
          println("Retrying chat completion due to $error after $attempts attempts")
        }
        .retry {
          httpClient.post {
            url { path("chat/completions") }
            configure(config.token, request)
            timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
          }
        }
    val body: ChatCompletionResponse = response.body()
    with(body.usage) {
      logger.debug {
        "OpenAI Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response: HttpResponse =
      config.retryConfig.schedule().retry {
        httpClient.post {
          url { path("embeddings") }
          configure(config.token, request)
          timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
        }
      }
    val body: EmbeddingResult = response.body()
    with(body.usage) {
      logger.debug {
        "OpenAI Tokens :: prompt: $promptTokens, completion: $completionTokens, total: $totalTokens"
      }
    }
    return body
  }

  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse {
    val response: HttpResponse =
      config.retryConfig.schedule().retry {
        httpClient.post {
          url { path("images/generations") }
          configure(config.token, request)
          timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
        }
      }
    return response.body()
  }
}
