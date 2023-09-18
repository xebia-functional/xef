package com.xebia.functional.xef.gcp.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.gcp.GcpClient
import com.xebia.functional.xef.gcp.GcpConfig
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.util.date.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class GcpCompletion(
  override val modelType: ModelType,
  config: GcpConfig,
) : Completion {

  private val client: GcpClient = autoClose { GcpClient(modelType.name, config) }

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: String =
      client.promptMessage(
        request.prompt,
        temperature = request.temperature,
        maxOutputTokens = request.maxTokens,
        topP = request.topP
      )
    return CompletionResult(
      UUID.generateUUID().toString(),
      client.modelId,
      getTimeMillis(),
      client.modelId,
      listOf(CompletionChoice(response, 0, null, null)),
      Usage.ZERO, // TODO: token usage - no information about usage provided by GCP codechat model
    )
  }
}
