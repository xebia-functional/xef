package com.xebia.functional.gpt4all

import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.llm.models.usage.Usage

object UniversalEmbeddings : com.xebia.functional.xef.llm.Embeddings, Embeddings {

    override val name: String = UniversalEmbeddings::class.java.canonicalName

    override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
        return EmbeddingResult(
          UniversalSentenceEncoder.embeddings(request.input).map {
            Embedding("embedding", it.toList(), 0)
          },
          Usage.ZERO
        )
    }

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<com.xebia.functional.xef.embeddings.Embedding> {
    return texts.flatMap { text ->
      UniversalSentenceEncoder.embeddings(listOf(text)).map {
        com.xebia.functional.xef.embeddings.Embedding(it.toList())
      }
    }
  }

  override suspend fun embedQuery(
    text: String,
    requestConfig: RequestConfig
  ): List<com.xebia.functional.xef.embeddings.Embedding> =
    embedDocuments(listOf(text), null, requestConfig)
}
