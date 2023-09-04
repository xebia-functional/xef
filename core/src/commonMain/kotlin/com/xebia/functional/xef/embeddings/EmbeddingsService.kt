package com.xebia.functional.xef.embeddings

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.llm.LLMEmbeddings
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

class EmbeddingsService(
  private val llmEmbeddings: LLMEmbeddings,
  private val chunkSize: Int = 400
) : Embeddings {

  override suspend fun embedDocuments(
    texts: List<String>,
    requestConfig: RequestConfig
  ): List<Embedding> {
    suspend fun createEmbeddings(texts: List<String>): List<Embedding> {
      val req = EmbeddingRequest(llmEmbeddings.name, texts, requestConfig.user.id)
      return llmEmbeddings.createEmbeddings(req).data.map { Embedding(it.embedding) }
    }
    val lists: List<List<Embedding>> =
      if (texts.isEmpty()) emptyList() else texts.chunked(chunkSize).parMap { createEmbeddings(it) }
    return lists.flatten()
  }
}
