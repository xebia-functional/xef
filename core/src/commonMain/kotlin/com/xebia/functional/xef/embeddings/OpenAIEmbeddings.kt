package com.xebia.functional.xef.embeddings

import arrow.fx.coroutines.parMap
import arrow.resilience.retry
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.EmbeddingRequest
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.llm.openai.RequestConfig
import io.github.oshai.KLogger
import kotlin.time.ExperimentalTime

@ExperimentalTime
class OpenAIEmbeddings(
  private val config: OpenAIConfig,
  private val oaiClient: OpenAIClient,
  private val logger: KLogger
) : Embeddings {

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding> = chunkedEmbedDocuments(texts, chunkSize ?: config.chunkSize, requestConfig)

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
    oaiClient.createEmbeddings(EmbeddingRequest(requestConfig.model.modelName, texts, requestConfig.user.id))
      .data
      .map { Embedding(it.embedding) }
}
