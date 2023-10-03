package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.embedding.Embedding as OpenAIEmbedding
import com.aallam.openai.api.embedding.embeddingRequest
import com.aallam.openai.api.model.ModelId
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult

class OpenAIEmbeddings(
  private val provider: OpenAI, // TODO: use context receiver
  override val modelType: ModelType,
) : Embeddings {

  private val client = provider.defaultClient

  override fun copy(modelType: ModelType) = OpenAIEmbeddings(provider, modelType)

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
