package com.xebia.functional.xef.data

import ai.xef.Embeddings
import com.xebia.functional.xef.llm.EmbeddingRequest
import com.xebia.functional.xef.llm.EmbeddingResponse
import com.xebia.functional.xef.llm.Usage

class TestEmbeddings : Embeddings, AutoCloseable {

  var requests: MutableList<EmbeddingRequest> = mutableListOf()

  override suspend fun createEmbedding(
    embeddingsRequest: EmbeddingRequest
  ): EmbeddingResponse {
    requests.add(embeddingsRequest)
    val response =
      EmbeddingResponse(
        embedding = emptyList(),
        usage = Usage(0, 0, 0)
      )
    return response
  }

  override val modelName: String = "fake-embeddings-model"

  override fun close() {}
}
