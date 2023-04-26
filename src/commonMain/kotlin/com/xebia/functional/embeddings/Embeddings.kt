package com.xebia.functional.embeddings

import com.xebia.functional.llm.openai.RequestConfig

data class Embedding(val data: List<Double>)

interface Embeddings {
  fun embedDocuments(texts: List<String>, chunkSize: Int?, config: RequestConfig): List<Embedding>
  fun embedQuery(text: String, config: RequestConfig): List<Embedding>
}
