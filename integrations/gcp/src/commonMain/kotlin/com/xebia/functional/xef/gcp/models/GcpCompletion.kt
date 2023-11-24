package com.xebia.functional.xef.gcp.models

import com.xebia.functional.xef.gcp.GCP
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.util.date.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class GcpCompletion(
  private val provider: GCP, // TODO: use context receiver
  override val modelID: ModelID,
  override val contextLength: MaxIoContextLength,
) : Completion {

  private val client = provider.defaultClient

  override fun copy(modelID: ModelID) = GcpCompletion(provider, modelID, contextLength)

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: String =
      client.promptMessage(
        modelID.value,
        request.prompt,
        temperature = request.temperature,
        maxOutputTokens = request.maxTokens,
        topP = request.topP
      )
    return CompletionResult(
      UUID.generateUUID().toString(),
      modelID.value,
      getTimeMillis(),
      modelID.value,
      listOf(CompletionChoice(response, 0, null, null)),
      Usage.ZERO, // TODO: token usage - no information about usage provided by GCP codechat model
    )
  }

  override fun countTokens(text: String): Int {
    TODO("Not yet implemented")
  }

  override fun truncateText(text: String, maxTokens: Int): String {
    TODO("Not yet implemented")
  }
}
