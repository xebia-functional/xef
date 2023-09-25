package com.xebia.functional.gpt4all

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.llm.models.usage.Usage

class HuggingFaceLocalEmbeddings(
  override val modelType: ModelType,
  artifact: String,
) : Embeddings {

  private val tokenizer = HuggingFaceTokenizer.newInstance("${modelType.name}/$artifact")

  override val name: String = HuggingFaceLocalEmbeddings::class.java.canonicalName

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val embedings = tokenizer.batchEncode(request.input)
    return EmbeddingResult(
      data = embedings.map { Embedding(it.ids.map { it.toFloat() }) },
      usage = Usage.ZERO
    )
  }

  override suspend fun embedDocuments(
    texts: List<String>,
    requestConfig: RequestConfig,
    chunkSize: Int?
  ): List<Embedding> =
    tokenizer.batchEncode(texts).map { em -> Embedding(em.ids.map { it.toFloat() }) } // TODO we need to remove the index

  companion object {
    @JvmField
    val DEFAULT = HuggingFaceLocalEmbeddings(ModelType.TODO("sentence-transformers"), artifact = "msmarco-distilbert-dot-v5")
  }
}
