package com.xebia.functional.llm.openai

import arrow.fx.coroutines.ResourceScope
import arrow.resilience.retry
import com.xebia.functional.configure
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.path

interface OpenAIClient {
  suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice>
  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse
  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult
}

suspend fun ResourceScope.KtorOpenAIClient(
  config: OpenAIConfig,
  engine: HttpClientEngine? = null
): OpenAIClient = KtorOpenAIClient(httpClient(engine, config.baseUrl), config)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val config: OpenAIConfig
) : OpenAIClient {

  override suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice> {
    val response = config.retryConfig.schedule().retry {
      httpClient.post {
        url { path("completions") }
        configure(config.token, request)
      }
    }
    val body: CompletionResult = response.body()
    return body.choices
  }

  override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
    val response = config.retryConfig.schedule()
      .log { error, attempts ->
        println("Retrying chat completion due to $error after $attempts attempts")
      }
      .retry {
      httpClient.post {
        url { path("chat/completions") }
        configure(config.token, request)
      }
    }
    return response.body()
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response: HttpResponse = config.retryConfig.schedule().retry {
      httpClient.post {
        url { path("embeddings") }
        configure(config.token, request)
      }
    }
    return response.body()
  }
}
