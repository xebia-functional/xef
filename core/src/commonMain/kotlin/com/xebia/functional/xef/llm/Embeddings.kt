package com.xebia.functional.xef.llm

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.models.CreateEmbeddingRequest
import com.xebia.functional.openai.models.CreateEmbeddingResponse
import com.xebia.functional.openai.models.Embedding
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestInput
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestModel

interface Embeddings : LLM {
  suspend fun createEmbeddings(request: CreateEmbeddingRequest): CreateEmbeddingResponse

  suspend fun embedDocuments(texts: List<String>, chunkSize: Int?): List<Embedding> =
    if (texts.isEmpty()) emptyList()
    else
      texts
        .chunked(chunkSize ?: 400)
        .parMap {
          createEmbeddings(
              CreateEmbeddingRequest(
                model = CreateEmbeddingRequestModel.valueOf(modelType.name),
                input = CreateEmbeddingRequestInput.StringArrayValue(it)
              )
            )
            .data
        }
        .flatten()

  suspend fun embedQuery(text: String): List<Embedding> =
    if (text.isNotEmpty()) embedDocuments(listOf(text), null) else emptyList()

  companion object
}
