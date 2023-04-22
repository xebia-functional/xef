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

interface OpenAIClient {
  suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice>
  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult
}

suspend fun ResourceScope.KtorOpenAIClient(
  engine: HttpClientEngine,
  config: OpenAIConfig
): OpenAIClient = KtorOpenAIClient(httpClient(engine), config)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val config: OpenAIConfig
) : OpenAIClient {

  private val baseUrl = "https://api.openai.com/v1"

  override suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice> {
    val response = config.retryConfig.schedule().retry {
      httpClient.post("$baseUrl/completions") { configure(config.token, request) }
    }
    return response.body()
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response = config.retryConfig.schedule().retry {
      httpClient.post("$baseUrl/embeddings") { configure(config.token, request) }
    }
    return response.body()
  }
}
