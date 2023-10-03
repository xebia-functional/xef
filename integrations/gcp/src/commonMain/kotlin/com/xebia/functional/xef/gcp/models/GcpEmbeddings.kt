package com.xebia.functional.xef.gcp.models

import com.xebia.functional.xef.gcp.GcpClient
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.usage.Usage

class GcpEmbeddings(
  override val modelID: ModelID,
  private val client: GcpClient,
) : Embeddings {

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    fun requestToEmbedding(it: GcpClient.EmbeddingPredictions): Embedding =
      Embedding(it.embeddings.values.map(Double::toFloat))

    val response = client.embeddings(request)
    return EmbeddingResult(
      data = response.predictions.map(::requestToEmbedding),
      usage = usage(response),
    )
  }

  private fun usage(response: GcpClient.EmbeddingResponse) =
    Usage(
      totalTokens = response.predictions.sumOf { it.embeddings.statistics.tokenCount },
      promptTokens = null,
      completionTokens = null,
    )
}
