package com.xebia.functional.xef.data

import com.xebia.functional.openai.generated.api.Embeddings
import com.xebia.functional.openai.generated.model.CreateEmbeddingRequest
import com.xebia.functional.openai.generated.model.CreateEmbeddingResponse
import com.xebia.functional.openai.generated.model.CreateEmbeddingResponseUsage
import io.ktor.client.request.*

class TestEmbeddings : Embeddings, AutoCloseable {

  var requests: MutableList<CreateEmbeddingRequest> = mutableListOf()

  override suspend fun createEmbedding(
    createEmbeddingRequest: CreateEmbeddingRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CreateEmbeddingResponse {
    requests.add(createEmbeddingRequest)
    val response =
      CreateEmbeddingResponse(
        data = emptyList(),
        model = "",
        `object` = CreateEmbeddingResponse.Object.list,
        usage = CreateEmbeddingResponseUsage(0, 0)
      )
    return response
  }

  override fun close() {}
}
