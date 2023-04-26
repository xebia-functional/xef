package com.xebia.functional.embeddings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class OpenAIEmbeddings(private val config: OpenAIConfig, private val oaiClient: OpenAIClient, private val logger: KotlinLogging) : Embeddings {

  override suspend fun embedQuery(text: String, rc: RequestConfig): List<Embedding> {
    return if (text.isNotEmpty()) embedDocuments(listOf(text), null, rc)
    else emptyList()
  }

  override suspend fun embedDocuments(texts: List<String>, chunkSize: Int?, rc: RequestConfig): List<Embedding> {
    return chunkedEmbedDocuments(texts, chunkSize ?: config.chunkSize, rc)
  }

  private suspend fun chunkedEmbedDocuments(texts: List<String>, chunkSize: Int, rc: RequestConfig): List<Embedding> {
    if (texts.isEmpty()) return emptyList()

    val batches = texts.chunked(chunkSize)
    val embeddings = mutableListOf<Embedding>()
    batches.forEach { batch ->
      val vectors = embedWithRetry(batch, rc)
      embeddings.addAll(vectors)
    }
    return embeddings
  }

  private suspend fun embedWithRetry(texts: List<String>, rc: RequestConfig): List<Embedding> {
    val result = retryingOnAllErrors(
      policy = limitRetries(config.maxRetries) + exponentialBackoff(config.backoff),
      onError = ::logError
    ) {
      oaiClient.createEmbeddings(EmbeddingRequest(rc.model.name, texts, rc.user.asString))
    }
    return result.data.map { Embedding(it.embedding) }
  }

  private suspend fun logError(err: Throwable, details: RetryDetails): Unit = when (details) {
    is WillDelayAndRetry -> {
      logger.warn { "Open AI call failed. So far we have retried ${details.retriesSoFar} times." }
    }
    is GivingUp -> {
      logger.warn { "Open AI call failed. Giving up after ${details.totalRetries} retries" }
    }
  }
}
