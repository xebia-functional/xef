package xef

import com.xebia.functional.xef.openapi.*
import io.ktor.client.request.*

class TestEmbeddings : Embeddings, AutoCloseable {

  var requests: MutableList<CreateEmbeddingRequest> = mutableListOf()

  override suspend fun createEmbedding(
    createEmbeddingRequest: CreateEmbeddingRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CreateEmbeddingResponse {
    requests.add(createEmbeddingRequest)
    val maybeTextInput = when(val input = createEmbeddingRequest.input) {
      is CreateEmbeddingRequest.Input.CaseStrings -> input.value.firstOrNull()
      is CreateEmbeddingRequest.Input.CaseString -> input.value
      else -> null
    }
    val data = when(maybeTextInput) {
      "fooz" -> listOf(
        Embedding(0, listOf(7.0, 8.0, 9.0), Embedding.Object.Embedding),
        Embedding(1, listOf(7.5, 8.5, 9.5), Embedding.Object.Embedding)
      )
      "foo" -> listOf(Embedding(0, listOf(1.0, 2.0, 3.0), Embedding.Object.Embedding))
      "bar" -> listOf(Embedding(0, listOf(4.0, 5.0, 6.0), Embedding.Object.Embedding))
      "baz" -> listOf()
      else -> listOf()
    }
    return CreateEmbeddingResponse(
      data = data,
      model = "test-model",
      `object` = CreateEmbeddingResponse.Object.List,
      usage = CreateEmbeddingResponse.Usage(0, 0)
    )
  }


  override fun close() {}
}
