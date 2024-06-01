package xef

import ai.xef.Embeddings
import com.xebia.functional.xef.llm.Embedding
import com.xebia.functional.xef.llm.EmbeddingRequest
import com.xebia.functional.xef.llm.EmbeddingResponse
import com.xebia.functional.xef.llm.Usage

class TestEmbeddings : Embeddings, AutoCloseable {

  var requests: MutableList<EmbeddingRequest> = mutableListOf()

  override suspend fun createEmbedding(
    embeddingsRequest: EmbeddingRequest
  ): EmbeddingResponse {
    requests.add(embeddingsRequest)
    val maybeTextInput = embeddingsRequest.text.firstOrNull()
    val data = when(maybeTextInput) {
      "fooz" -> listOf(
        Embedding(listOf(7.0f, 8.0f, 9.0f)),
        Embedding(listOf(7.5f, 8.5f, 9.5f))
      )
      "foo" -> listOf(Embedding(listOf(1.0f, 2.0f, 3.0f)))
      "bar" -> listOf(Embedding(listOf(4.0f, 5.0f, 6.0f)))
      "baz" -> listOf()
      else -> listOf()
    }
    return EmbeddingResponse(
      embedding = data,
      usage = Usage(0, 0, 0)
    )
  }

  override val modelName: String = "fake-embeddings-model"


  override fun close() {}
}
