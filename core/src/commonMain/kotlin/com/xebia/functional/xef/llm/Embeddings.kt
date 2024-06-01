package com.xebia.functional.xef.llm

import ai.xef.Embeddings
import arrow.fx.coroutines.parMap


data class EmbeddingRequest(
  val text: List<String>,
  val model: Embeddings
)

data class EmbeddingResponse(
  val embedding: List<Embedding>,
  val usage: Usage
)

data class Embedding(
  val embedding: List<Float>
)

suspend fun Embeddings.embedDocuments(
  texts: List<String>,
  chunkSize: Int = 400,
): List<Embedding> =
  if (texts.isEmpty()) emptyList()
  else
    texts
      .chunked(chunkSize)
      .parMap(concurrency = 3) {
        createEmbedding(
            EmbeddingRequest(
              model = this@embedDocuments,
              text = it
            )
          )
          .embedding
      }
      .flatten()

suspend fun Embeddings.embedQuery(
  text: String,
): List<Embedding> =
  if (text.isNotEmpty())
    embedDocuments(texts = listOf(text))
  else emptyList()
