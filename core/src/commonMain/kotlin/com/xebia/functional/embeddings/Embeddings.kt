package com.xebia.functional.embeddings

import com.xebia.functional.llm.openai.RequestConfig
import kotlin.math.sqrt

data class Embedding(val data: List<Float>)

interface Embeddings {
  suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding>
  suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding>

  companion object
}

public fun Embedding.cosineSimilarity(other: Embedding): Double {
  val dotProduct = this.data.zip(other.data).sumOf { (a, b) -> (a * b).toDouble() }
  val magnitudeA = sqrt(this.data.sumOf { (it * it).toDouble() })
  val magnitudeB = sqrt(other.data.sumOf { (it * it).toDouble() })
  return dotProduct / (magnitudeA * magnitudeB)
}
