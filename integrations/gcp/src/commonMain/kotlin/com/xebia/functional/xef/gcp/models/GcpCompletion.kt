package com.xebia.functional.xef.gcp.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.gcp.GCP
import com.xebia.functional.xef.gcp.GcpClient
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.util.date.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class GcpCompletion(
  private val provider: GCP, //TODO: use context receiver
  override val modelType: ModelType,
) : Completion {

  private val client = provider.defaultClient

  override fun copy(modelType: ModelType) =
    GcpCompletion(provider, modelType)

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: String =
      client.promptMessage(
        modelType.name,
        request.prompt,
        temperature = request.temperature,
        maxOutputTokens = request.maxTokens,
        topP = request.topP
      )
    return CompletionResult(
      UUID.generateUUID().toString(),
      modelType.name,
      getTimeMillis(),
      modelType.name,
      listOf(CompletionChoice(response, 0, null, null)),
      Usage.ZERO, // TODO: token usage - no information about usage provided by GCP codechat model
    )
  }
}
