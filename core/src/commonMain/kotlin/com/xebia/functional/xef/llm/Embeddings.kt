package com.xebia.functional.xef.llm

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

interface Embeddings : LLM {
  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult

  suspend fun embedDocuments(
    texts: List<String>,
    requestConfig: RequestConfig,
    chunkSize: Int?
  ): List<Embedding> {
    suspend fun createEmbeddings(texts: List<String>): List<Embedding> {
      val req = EmbeddingRequest(name, texts, requestConfig.user.id)
      return createEmbeddings(req).data.map { Embedding(it.embedding) }
    }
    val lists: List<List<Embedding>> =
      if (texts.isEmpty()) emptyList()
      else texts.chunked(chunkSize ?: 400).parMap { createEmbeddings(it) }
    return lists.flatten()
  }

  suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
    if (text.isNotEmpty()) embedDocuments(listOf(text), requestConfig, null) else emptyList()

  companion object
}
