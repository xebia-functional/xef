package com.xebia.functional.embeddings

import com.xebia.functional.llm.openai.RequestConfig

fun Embeddings.Companion.mock(
  embedDocuments:
    suspend (texts: List<String>, chunkSize: Int?, config: RequestConfig) -> List<Embedding> =
    { _, _, _ ->
      listOf(Embedding(listOf(1.0f, 2.0f, 3.0f)), Embedding(listOf(4.0f, 5.0f, 6.0f)))
    },
  embedQuery: suspend (text: String, config: RequestConfig) -> List<Embedding> = { text, _ ->
    when (text) {
      "foo" -> listOf(Embedding(listOf(1.0f, 2.0f, 3.0f)))
      "bar" -> listOf(Embedding(listOf(4.0f, 5.0f, 6.0f)))
      "baz" -> listOf()
      else -> listOf()
    }
  }
): Embeddings =
  object : Embeddings {
    override suspend fun embedDocuments(
      texts: List<String>,
      chunkSize: Int?,
      requestConfig: RequestConfig
    ): List<Embedding> = embedDocuments(texts, chunkSize, requestConfig)

    override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
      embedQuery(text, requestConfig)
  }
