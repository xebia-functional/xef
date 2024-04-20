package xef

import com.xebia.functional.openai.CompletableFuture
import com.xebia.functional.openai.generated.api.Embeddings
import com.xebia.functional.openai.generated.model.*
import io.ktor.client.request.*

class TestEmbeddings : Embeddings, AutoCloseable {

  var requests: MutableList<CreateEmbeddingRequest> = mutableListOf()

  override fun createEmbeddingAsync(
    createEmbeddingRequest: CreateEmbeddingRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CompletableFuture<CreateEmbeddingResponse> {
    TODO("Not yet implemented")
  }

  override suspend fun createEmbedding(
    createEmbeddingRequest: CreateEmbeddingRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CreateEmbeddingResponse {
    requests.add(createEmbeddingRequest)
    val maybeTextInput = when(val input = createEmbeddingRequest.input) {
      is CreateEmbeddingRequestInput.CaseStrings -> input.value.firstOrNull()
      is CreateEmbeddingRequestInput.CaseString -> input.value
      else -> null
    }
    val data = when(maybeTextInput) {
      "fooz" -> listOf(
        Embedding(0, listOf(7.0, 8.0, 9.0), Embedding.Object.embedding),
        Embedding(1, listOf(7.5, 8.5, 9.5), Embedding.Object.embedding)
      )
      "foo" -> listOf(Embedding(0, listOf(1.0, 2.0, 3.0), Embedding.Object.embedding))
      "bar" -> listOf(Embedding(0, listOf(4.0, 5.0, 6.0), Embedding.Object.embedding))
      "baz" -> listOf()
      else -> listOf()
    }
    return CreateEmbeddingResponse(
      data = data,
      model = "test-model",
      `object` = CreateEmbeddingResponse.Object.list,
      usage = CreateEmbeddingResponseUsage(0, 0)
    )
  }


  override fun close() {}
}
