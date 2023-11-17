package xef

import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.infrastructure.HttpResponse
import com.xebia.functional.openai.models.*
import com.xebia.functional.xef.utils.TestBodyProvider
import com.xebia.functional.xef.utils.TestHttpResponse
import kotlin.coroutines.CoroutineContext

class TestEmbeddings(private val context: CoroutineContext) : EmbeddingsApi(), AutoCloseable {

  var requests: MutableList<CreateEmbeddingRequest> = mutableListOf()

  override suspend fun createEmbedding(
    createEmbeddingRequest: CreateEmbeddingRequest
  ): HttpResponse<CreateEmbeddingResponse> {
    requests.add(createEmbeddingRequest)
    val response =
      CreateEmbeddingResponse(
        data = emptyList(),
        model = "",
        `object` = CreateEmbeddingResponse.Object.list,
        usage = CreateEmbeddingResponseUsage(0, 0)
      )
    return HttpResponse(TestHttpResponse(context, 200), TestBodyProvider(response))
  }

  override fun close() {}
}
