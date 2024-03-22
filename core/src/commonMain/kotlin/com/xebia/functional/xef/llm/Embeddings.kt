package com.xebia.functional.xef.llm

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateEmbeddingRequest
import com.xebia.functional.openai.models.CreateEmbeddingRequestModel
import com.xebia.functional.openai.models.Embedding
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestInput

suspend fun EmbeddingsApi.embedDocuments(
  texts: List<String>,
  chunkSize: Int = 400,
  embeddingRequestModel: OpenAIModel<CreateEmbeddingRequestModel> =
    StandardModel(CreateEmbeddingRequestModel.text_embedding_ada_002)
): List<Embedding> =
  if (texts.isEmpty()) emptyList()
  else
    texts
      .chunked(chunkSize)
      .parMap {
        createEmbedding(
            CreateEmbeddingRequest(
              model = embeddingRequestModel,
              input = CreateEmbeddingRequestInput.StringArrayValue(it)
            )
          )
          .body()
          .data
      }
      .flatten()

suspend fun EmbeddingsApi.embedQuery(
  text: String,
  embeddingRequestModel: OpenAIModel<CreateEmbeddingRequestModel>
): List<Embedding> =
  if (text.isNotEmpty())
    embedDocuments(texts = listOf(text), embeddingRequestModel = embeddingRequestModel)
  else emptyList()
