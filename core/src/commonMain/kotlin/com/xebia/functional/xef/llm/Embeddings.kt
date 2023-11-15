package com.xebia.functional.xef.llm

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateEmbeddingRequest
import com.xebia.functional.openai.models.CreateEmbeddingRequestModel
import com.xebia.functional.openai.models.Embedding
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestInput

suspend fun EmbeddingsApi.embedDocuments(texts: List<String>, chunkSize: Int?): List<Embedding> =
  if (texts.isEmpty()) emptyList()
  else
    texts
      .chunked(chunkSize ?: 400)
      .parMap {
        createEmbedding(
          CreateEmbeddingRequest(
            model = ai.xef.openai.StandardModel(CreateEmbeddingRequestModel.textMinusEmbeddingMinusAdaMinus002),
            input = CreateEmbeddingRequestInput.StringArrayValue(it)
          )
        ).body().data
      }
      .flatten()

suspend fun EmbeddingsApi.embedQuery(text: String): List<Embedding> =
  if (text.isNotEmpty()) embedDocuments(listOf(text), null) else emptyList()

