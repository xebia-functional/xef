package com.xebia.functional.xef.llm.openai

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.AIClient
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import kotlin.time.ExperimentalTime

@ExperimentalTime
class OpenAIEmbeddings(private val oaiClient: AIClient) : Embeddings {

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding> = chunkedEmbedDocuments(texts, chunkSize ?: 200, requestConfig)

  override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
    if (text.isNotEmpty()) embedDocuments(listOf(text), null, requestConfig) else emptyList()

  private suspend fun chunkedEmbedDocuments(
    texts: List<String>,
    chunkSize: Int,
    requestConfig: RequestConfig
  ): List<Embedding> =
    if (texts.isEmpty()) emptyList()
    else texts.chunked(chunkSize).parMap { createEmbeddingWithRetry(it, requestConfig) }.flatten()

  private suspend fun createEmbeddingWithRetry(
    texts: List<String>,
    requestConfig: RequestConfig
  ): List<Embedding> =
    oaiClient
      .createEmbeddings(
        EmbeddingRequest(requestConfig.model.modelName, texts, requestConfig.user.id)
      )
      .data
      .map { Embedding(it.embedding) }
}
