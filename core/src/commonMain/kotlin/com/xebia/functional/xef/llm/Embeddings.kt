package com.xebia.functional.xef.llm

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.generated.api.Embeddings
import com.xebia.functional.openai.generated.model.CreateEmbeddingRequest
import com.xebia.functional.openai.generated.model.CreateEmbeddingRequestInput
import com.xebia.functional.openai.generated.model.CreateEmbeddingRequestModel
import com.xebia.functional.openai.generated.model.Embedding

suspend fun Embeddings.embedDocuments(
  texts: List<String>,
  chunkSize: Int = 400,
  embeddingRequestModel: CreateEmbeddingRequestModel =
    CreateEmbeddingRequestModel.text_embedding_ada_002
): List<Embedding> =
  if (texts.isEmpty()) emptyList()
  else
    texts
      .chunked(chunkSize)
      .parMap {
        createEmbedding(
            CreateEmbeddingRequest(
              model = embeddingRequestModel,
              input = CreateEmbeddingRequestInput.CaseStrings(it)
            )
          )
          .data
      }
      .flatten()

suspend fun Embeddings.embedQuery(
  text: String,
  embeddingRequestModel: CreateEmbeddingRequestModel
): List<Embedding> =
  if (text.isNotEmpty())
    embedDocuments(texts = listOf(text), embeddingRequestModel = embeddingRequestModel)
  else emptyList()
