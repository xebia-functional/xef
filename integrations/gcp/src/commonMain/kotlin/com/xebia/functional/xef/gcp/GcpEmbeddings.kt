package com.xebia.functional.xef.gcp

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

class GcpEmbeddings(private val gcpClient: com.xebia.functional.xef.llm.Embeddings) : Embeddings {
  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding> {
    suspend fun createEmbeddings(texts: List<String>): List<Embedding> {
      val req = EmbeddingRequest(gcpClient.name, texts, requestConfig.user.id)
      return gcpClient.createEmbeddings(req).data.map { Embedding(it.embedding) }
    }
    val lists: List<List<Embedding>> =
      if (texts.isEmpty()) emptyList()
      else texts.chunked(chunkSize ?: 400).parMap { createEmbeddings(it) }
    return lists.flatten()
  }

  override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
    if (text.isNotEmpty()) embedDocuments(listOf(text), null, requestConfig) else emptyList()
}
