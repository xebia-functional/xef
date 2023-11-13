package com.xebia.functional.xef.data

import com.xebia.functional.openai.models.CreateEmbeddingRequest
import com.xebia.functional.openai.models.CreateEmbeddingResponse
import com.xebia.functional.openai.models.CreateEmbeddingResponseUsage
import com.xebia.functional.openai.models.Embedding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Embeddings

class TestEmbeddings : Embeddings {

  override val modelType: ModelType = ModelType.TODO("test-embeddings")

  override fun copy(modelType: ModelType) = TestEmbeddings()

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?
  ): List<Embedding> = emptyList()

  override suspend fun embedQuery(text: String): List<Embedding> =
    emptyList()

  override suspend fun createEmbeddings(request: CreateEmbeddingRequest): CreateEmbeddingResponse =
    CreateEmbeddingResponse(emptyList(), "", CreateEmbeddingResponse.Object.list, CreateEmbeddingResponseUsage(0, 0))
}
