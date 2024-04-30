package com.xebia.functional.xef.evaluator.models

import com.xebia.functional.xef.llm.models.MessageWithUsage
import com.xebia.functional.xef.llm.models.MessagesUsage
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable

@Serializable data class OutputDescription(val value: String)

@Serializable
data class OutputResponse(
  val description: OutputDescription,
  val tokens: OutputTokens?,
  val value: String
) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: OutputDescription,
      block: suspend () -> MessageWithUsage
    ): OutputResponse {
      val response = block()
      return OutputResponse(description, response.usage?.let { OutputTokens(it) }, response.message)
    }
  }
}

@Serializable
data class OutputTokens(
  val promptTokens: Int? = null,
  val completionTokens: Int? = null,
  val totalTokens: Int? = null
) {
  companion object {
    @JvmSynthetic
    operator fun invoke(usage: MessagesUsage): OutputTokens =
      OutputTokens(usage.promptTokens, usage.completionTokens, usage.totalTokens)
  }
}
