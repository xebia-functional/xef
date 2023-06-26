package com.xebia.functional.xef.llm.openai

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.AIClient
import com.xebia.functional.xef.llm.openai.EmbeddingRequest
import com.xebia.functional.xef.llm.openai.RequestConfig
import kotlin.time.ExperimentalTime

@ExperimentalTime
class OpenAIEmbeddings(private val config: OpenAIConfig, private val oaiClient: AIClient) :
  Embeddings {

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding> {
    suspend fun createEmbeddings(texts: List<String>): List<Embedding> {
      val req = EmbeddingRequest(requestConfig.model.modelName, texts, requestConfig.user.id)
      return oaiClient.createEmbeddings(req).data.map { Embedding(it.embedding) }
    }
    val lists: List<List<Embedding>> =
      if (texts.isEmpty()) emptyList()
      else texts.chunked(chunkSize ?: config.chunkSize).parMap { createEmbeddings(it) }
    return lists.flatten()
  }

  override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
    if (text.isNotEmpty()) embedDocuments(listOf(text), null, requestConfig) else emptyList()
}
