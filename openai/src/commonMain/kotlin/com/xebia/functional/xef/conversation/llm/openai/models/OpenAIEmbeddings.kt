package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.embedding.Embedding as OpenAIEmbedding
import com.aallam.openai.api.embedding.embeddingRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult

class OpenAIEmbeddings(
  override val modelID: com.xebia.functional.xef.llm.models.ModelID,
  private val client: OpenAI,
  override val encodingType: EncodingType,
) : Embeddings, OpenAIModel {

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val clientRequest = embeddingRequest {
      model = ModelId(request.model)
      input = request.input
      user = request.user
    }

    fun createEmbedding(it: OpenAIEmbedding): Embedding =
      Embedding(it.embedding.map { it.toFloat() })

    val response = client.embeddings(clientRequest)
    return EmbeddingResult(
      data = response.embeddings.map { createEmbedding(it) },
      usage = response.usage.toInternal()
    )
  }
}
