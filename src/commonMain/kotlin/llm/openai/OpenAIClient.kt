package llm.openai

import arrow.fx.coroutines.ResourceScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

interface OpenAIClient {
  suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice>
  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult
}

suspend fun ResourceScope.KtorOpenAIClient(
  engine: HttpClientEngine,
  token: String
): OpenAIClient = KtorOpenAIClient(httpClient(engine), token)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val token: String
) : OpenAIClient {

  private val baseUrl = "https://api.openai.com/v1"

  override suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice> {
    val response = httpClient.post("$baseUrl/completions") { configure(request) }
    return response.body()
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response = httpClient.post("$baseUrl/embeddings") { configure(request) }
    return response.body()
  }

  private inline fun <reified A> HttpRequestBuilder.configure(request: A): Unit {
    header("Authorization", "Bearer $token")
    contentType(ContentType.Application.Json)
    setBody(request)
  }
}

private suspend fun ResourceScope.httpClient(engine: HttpClientEngine): HttpClient =
  install({
    HttpClient(engine) {
      install(ContentNegotiation) { json() }
    }
  }) { client, _ -> client.close() }