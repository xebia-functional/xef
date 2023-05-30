package com.xebia.functional.xef.llm.openai

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.nonFatalOrThrow
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import arrow.fx.coroutines.ResourceScope
import arrow.resilience.Schedule
import arrow.resilience.ScheduleStep
import arrow.resilience.retry
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.AIError.AIClient.FailedParsing
import com.xebia.functional.xef.configure
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.httpClient
import com.xebia.functional.xef.llm.AIClientError
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.statement.*
import io.ktor.http.path
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val logger: KLogger = KotlinLogging.logger {}

interface OpenAIClient {
  suspend fun createCompletion(request: CompletionRequest): CompletionResult
  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse
  suspend fun createEmbeddings(request: EmbeddingRequest): Either<AIError.AIClient, EmbeddingResult>
  suspend fun createImages(request: ImagesGenerationRequest): Either<FailedParsing, ImagesGenerationResponse>
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
  engine: HttpClientEngine? = null
): OpenAIClient = KtorOpenAIClient(httpClient(engine, config.baseUrl), config)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val config: OpenAIConfig,
  private val temperature: Double
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
    val response: HttpResponse =
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

  override suspend fun createEmbeddings(request: EmbeddingRequest): Either<AIError.AIClient, EmbeddingResult> {
    val history: NonEmptyList<History<HttpResponse>> =
      config.retryConfig.schedule().retryTimed {
        httpClient.post {
          url { path("embeddings") }
          configure(config.token, request)
          timeout { requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds }
        }
      }
    return history.head.result.fold({
      throw it
    }, { response ->
      response.bodyOrError<EmbeddingResult>()
        .onRight { with(it.usage) { logger.debug { "Embeddings Tokens :: total: $totalTokens" } } }
    })
  }

  override suspend fun createImages(request: ImagesGenerationRequest): Either<FailedParsing, ImagesGenerationResponse> {
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

private suspend inline fun <reified T> HttpResponse.bodyOrError(): Either<FailedParsing, T> {
  val contents = bodyAsText()
  return try {
    JsonLenient.decodeFromString<T>(contents).right()
  } catch (e: IllegalArgumentException) {
    FailedParsing(JsonLenient.decodeFromString<JsonElement>(contents), e).left()
  }
}

data class History<A>(val result: Either<Throwable, A>, val duration: Duration)

@OptIn(ExperimentalTime::class)
private suspend fun <Input, Output> Schedule<Throwable, Output>.retryTimed(action: suspend () -> Input): NonEmptyList<History<Input>> {
  var step: ScheduleStep<Throwable, Output> = step
  val history: MutableList<History<Input>> = mutableListOf()

  while (true) {
    currentCoroutineContext().ensureActive()
    val start = TimeSource.Monotonic.markNow()
    try {
      history.add(History(action.invoke().right(), start.elapsedNow()))
      return history.asReversed().toNonEmptyListOrNull()!!
    } catch (e: Throwable) {
      when (val decision = step(e.nonFatalOrThrow())) {
        is Schedule.Decision.Continue -> {
          history.add(History(e.left(), start.elapsedNow()))
          if (decision.delay != Duration.ZERO) delay(decision.delay)
          step = decision.step
        }

        is Schedule.Decision.Done -> {
          history.add(History(e.left(), start.elapsedNow()))
          return history.asReversed().toNonEmptyListOrNull()!!
        }
      }
    }
  }
}
