package com.xebia.functional.xef.mlflow.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.mlflow.MLflow

class MLflowEmbeddings(
  private val provider: MLflow, // TODO: use context receiver
  override val modelType: ModelType,
) : Embeddings {

  private val client = provider.defaultClient

  override fun copy(modelType: ModelType) = MLflowEmbeddings(provider, modelType)

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response =
      client.embeddings(
        // TODO - The model name used by MLflow is just an identifier
        modelType.name,
        request.input
      )
    return EmbeddingResult(
      response.toEmbeddings(),
      response.metadata.toUsage(),
    )
  }
}
