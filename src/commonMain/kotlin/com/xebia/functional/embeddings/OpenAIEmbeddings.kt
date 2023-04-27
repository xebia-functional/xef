package com.xebia.functional.embeddings

import arrow.fx.coroutines.parMap
import arrow.resilience.retry
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.EmbeddingRequest
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.RequestConfig
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
  ): List<Embedding> =
    chunkedEmbedDocuments(texts, chunkSize ?: config.chunkSize, requestConfig)

  override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
    if (text.isNotEmpty()) embedDocuments(listOf(text), null, requestConfig) else emptyList()

  private suspend fun chunkedEmbedDocuments(
    texts: List<String>,
    chunkSize: Int,
    requestConfig: RequestConfig
  ): List<Embedding> =
    if (texts.isEmpty()) emptyList()
    else texts.chunked(chunkSize)
      .parMap { createEmbeddingWithRetry(it, requestConfig) }
      .flatten()

  private suspend fun createEmbeddingWithRetry(texts: List<String>, requestConfig: RequestConfig): List<Embedding> =
    kotlin.runCatching {
      config.retryConfig.schedule()
        .log { retriesSoFar, _ -> logger.warn { "Open AI call failed. So far we have retried $retriesSoFar times." } }
        .retry {
          oaiClient.createEmbeddings(EmbeddingRequest(requestConfig.model.name, texts, requestConfig.user.id))
            .data.map { Embedding(it.embedding) }
        }
    }.getOrElse {
      logger.warn { "Open AI call failed. Giving up after ${config.retryConfig.maxRetries} retries" }
      throw it
    }
}