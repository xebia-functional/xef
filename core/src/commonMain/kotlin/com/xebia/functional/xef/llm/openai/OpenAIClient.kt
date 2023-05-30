package com.xebia.functional.xef.llm.openai

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.right
import arrow.fx.coroutines.ResourceScope
import arrow.resilience.retry
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.AIError.Client.FailedParsing
import com.xebia.functional.xef.Tracer
import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.httpClient
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.statement.*
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

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

@Serializable
data class ImageGenerationUrl(val url: String)

suspend fun ResourceScope.KtorOpenAIClient(
  config: OpenAIConfig,
  engine: HttpClientEngine? = null,
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
      config.retryConfig
        .schedule()
        .log { error, attempts -> logger.debug(error) { "Retrying chat completion after $attempts attempts" } }
        .retry {
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
    val response =
      config.retryConfig.schedule().retry {
        httpClient.post {
          url { path("embeddings") }
          configure(config.token, request)
          timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
        }
      }
    return response.bodyOrError()
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
    return response.bodyOrError()
  }
}

val JsonLenient = Json {
  isLenient = true
  ignoreUnknownKeys = true
}

private suspend inline fun <reified T> HttpResponse.bodyOrError(): T {
  val contents = bodyAsText()
//  return catch({
    return JsonLenient.decodeFromString<T>(contents)
//  }) { e: IllegalArgumentException ->
//    FailedParsing(JsonLenient.decodeFromString<JsonElement>(contents), e).left()
//  }
}
