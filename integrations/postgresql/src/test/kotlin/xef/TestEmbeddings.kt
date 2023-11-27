package xef

import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.infrastructure.HttpResponse
import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestInput
import com.xebia.functional.xef.utils.TestBodyProvider
import com.xebia.functional.xef.utils.TestHttpResponse
import kotlin.coroutines.CoroutineContext

class TestEmbeddings(private val context: CoroutineContext) : EmbeddingsApi(), AutoCloseable {

  var requests: MutableList<CreateEmbeddingRequest> = mutableListOf()

  override suspend fun createEmbedding(
    createEmbeddingRequest: CreateEmbeddingRequest
  ): HttpResponse<CreateEmbeddingResponse> {
    requests.add(createEmbeddingRequest)
    val maybeTextInput = when(val input = createEmbeddingRequest.input) {
      is CreateEmbeddingRequestInput.StringArrayValue -> input.v.firstOrNull()
      is CreateEmbeddingRequestInput.StringValue -> input.v
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
    val response =
      CreateEmbeddingResponse(
        data = data,
        model = "test-model",
        `object` = CreateEmbeddingResponse.Object.list,
        usage = CreateEmbeddingResponseUsage(0, 0)
      )
    return HttpResponse(TestHttpResponse(context, 200), TestBodyProvider(response))
  }

  override fun close() {}
}
