package com.xebia.functional.xef.embeddings

import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

data class Embedding(val data: List<Float>)

interface Embeddings {
  suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding>

  suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding>

  companion object
}
