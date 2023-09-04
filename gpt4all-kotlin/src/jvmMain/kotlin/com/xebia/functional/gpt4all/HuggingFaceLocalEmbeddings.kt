package com.xebia.functional.gpt4all

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.LLMEmbeddings
import com.xebia.functional.xef.llm.models.embeddings.LLMEmbedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.llm.models.usage.Usage

class HuggingFaceLocalEmbeddings(name: String, artifact: String) : LLMEmbeddings, Embeddings {

  private val tokenizer = HuggingFaceTokenizer.newInstance("$name/$artifact")

  override val name: String = HuggingFaceLocalEmbeddings::class.java.canonicalName

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val embedings = tokenizer.batchEncode(request.input)
    return EmbeddingResult(
      data = embedings.mapIndexed { n, em -> LLMEmbedding("embedding", em.ids.map { it.toFloat() }, n) },
      usage = Usage.ZERO
    )
  }

  override suspend fun embedDocuments(
    texts: List<String>,
    requestConfig: RequestConfig
  ): List<Embedding> =
    tokenizer.batchEncode(texts).map { em -> Embedding(em.ids.map { it.toFloat() }) }

  companion object {
    @JvmField
    val DEFAULT = HuggingFaceLocalEmbeddings("sentence-transformers", "msmarco-distilbert-dot-v5")
  }
}
