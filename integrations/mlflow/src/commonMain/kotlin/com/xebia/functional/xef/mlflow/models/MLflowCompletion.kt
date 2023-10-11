package com.xebia.functional.xef.mlflow.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.mlflow.MLflow
import com.xebia.functional.xef.mlflow.MlflowClient
import io.ktor.util.date.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class MLflowCompletion(
  private val provider: MLflow, // TODO: use context receiver
  override val modelType: ModelType,
) : Completion {

  private val client = provider.defaultClient

  override fun copy(modelType: ModelType) = MLflowCompletion(provider, modelType)

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: MlflowClient.PromptResponse =
      client.prompt(
        // TODO - The model name used by MLflow is just an identifier
        modelType.name,
        request.prompt,
        request.n,
        request.temperature,
        request.maxTokens,
        request.stop
      )
    return CompletionResult(
      UUID.generateUUID().toString(),
      modelType.name,
      getTimeMillis(),
      modelType.name,
      response.toCompletionChoices(),
      response.metadata.toUsage()
    )
  }
}
