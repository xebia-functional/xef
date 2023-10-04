package com.xebia.functional.xef.mlflow.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.mlflow.MLflow
import com.xebia.functional.xef.mlflow.MLflowClient
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class MLflowChat(
  private val provider: MLflow, // TODO: use context receiver
  override val modelType: ModelType,
) : Chat {

  private val client = provider.defaultClient

  override fun copy(modelType: ModelType) = MLflowChat(provider, modelType)

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val response: MLflowClient.ChatResponse =
      client.chat(
        // TODO - The model name used by MLflow is just an identifier
        modelType.name,
        request.messages.buildPrompt(),
        request.n,
        request.temperature,
        request.maxTokens,
        request.stop
      )
    return ChatCompletionResponse(
      UUID.generateUUID().toString(),
      response.metadata.model,
      getTimeMillis().toInt(),
      response.metadata.model,
      response.metadata.toUsage(),
      response.toChoices()
    )
  }

  /** MLFlow currently doesn't support streaming responses */
  override suspend fun createChatCompletions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> =
    with(request) {
      return flow {
        val response: MLflowClient.ChatResponse =
          client.chat(
            // TODO - The model name used by MLflow is just an identifier
            modelType.name,
            request.messages.buildPrompt(),
            request.n,
            request.temperature,
            request.maxTokens,
            request.stop
          )
        emit(
          ChatCompletionChunk(
            UUID.generateUUID().toString(),
            getTimeMillis().toInt(),
            response.metadata.model,
            response.toChunks(),
            response.metadata.toUsage()
          )
        )
      }
    }
}
