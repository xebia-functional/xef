package com.xebia.functional.xef.data

import io.github.nomisrev.openapi.CreateEmbeddingRequest
import io.github.nomisrev.openapi.CreateEmbeddingResponse
import io.github.nomisrev.openapi.Embeddings
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
        `object` = CreateEmbeddingResponse.Object.List,
        usage = CreateEmbeddingResponse.Usage(0, 0)
      )
    return response
  }

  override fun close() {}
}
