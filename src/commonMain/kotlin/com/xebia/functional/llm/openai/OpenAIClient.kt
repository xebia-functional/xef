package llm.openai

import arrow.fx.coroutines.ResourceScope
import com.xebia.functional.configure
import com.xebia.functional.httpClient
import com.xebia.functional.llm.openai.CompletionChoice
import com.xebia.functional.llm.openai.CompletionRequest
import com.xebia.functional.llm.openai.EmbeddingRequest
import com.xebia.functional.llm.openai.EmbeddingResult
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
  token: String
): OpenAIClient = KtorOpenAIClient(httpClient(engine), token)

private class KtorOpenAIClient(
  private val httpClient: HttpClient,
  private val token: String
) : OpenAIClient {

  private val baseUrl = "https://api.openai.com/v1"

  override suspend fun createCompletion(request: CompletionRequest): List<CompletionChoice> {
    val response = httpClient.post("$baseUrl/completions") { configure(token, request) }
    return response.body()
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response = httpClient.post("$baseUrl/embeddings") { configure(token, request) }
    return response.body()
  }
}
