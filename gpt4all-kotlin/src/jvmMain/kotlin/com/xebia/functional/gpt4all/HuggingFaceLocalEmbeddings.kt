package com.xebia.functional.gpt4all

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import com.xebia.functional.xef.embeddings.Embedding as XefEmbedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.llm.models.usage.Usage

class HuggingFaceLocalEmbeddings(name: String, artifact: String) : com.xebia.functional.xef.llm.Embeddings, Embeddings {

  private val tokenizer = HuggingFaceTokenizer.newInstance("$name/$artifact")

  override val name: String = HuggingFaceLocalEmbeddings::class.java.canonicalName

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val embeddings = tokenizer.batchEncode(request.input)
    return EmbeddingResult(
      data = embedings.mapIndexed { n, em -> Embedding("embedding", em.ids.map { it.toFloat() }, n) },
      usage = Usage.ZERO
    )
  }

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<XefEmbedding> =
    tokenizer.batchEncode(texts).mapIndexed { n, em ->
      XefEmbedding(em.ids.map { it.toFloat() })
    }

  override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<XefEmbedding> =
    embedDocuments(listOf(text), null, requestConfig)

  companion object {
    @JvmField
    val DEFAULT = HuggingFaceLocalEmbeddings("sentence-transformers", "msmarco-distilbert-dot-v5")
  }
}
