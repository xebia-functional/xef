package com.xebia.functional.xef.llm

import arrow.fx.coroutines.parMap
import io.github.nomisrev.openapi.*

suspend fun Embeddings.embedDocuments(
  texts: List<String>,
  chunkSize: Int = 400,
  embeddingRequestModel: CreateEmbeddingRequest.Model =
    CreateEmbeddingRequest.Model.TextEmbeddingAda002
): List<Embedding> =
  if (texts.isEmpty()) emptyList()
  else
    texts
      .chunked(chunkSize)
      .parMap {
        createEmbedding(
            CreateEmbeddingRequest(
              model = embeddingRequestModel,
              input = CreateEmbeddingRequest.Input.CaseStrings(it)
            )
          )
          .data
      }
      .flatten()

suspend fun Embeddings.embedQuery(
  text: String,
  embeddingRequestModel: CreateEmbeddingRequest.Model
): List<Embedding> =
  if (text.isNotEmpty())
    embedDocuments(texts = listOf(text), embeddingRequestModel = embeddingRequestModel)
  else emptyList()
