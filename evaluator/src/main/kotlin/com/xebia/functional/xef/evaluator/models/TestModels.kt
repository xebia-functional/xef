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
      price: ModelsPricing?,
      block: suspend () -> MessageWithUsage
    ): OutputResponse {
      val response = block()
      return OutputResponse(
        description,
        response.usage?.let { OutputTokens(it, price) },
        response.message
      )
    }
  }
}

@Serializable
data class OutputTokens(
  val promptTokens: Int? = null,
  val estimatePricePerToken: Double? = null,
  val completionTokens: Int? = null,
  val estimatePriceCompletionToken: Double? = null,
  val totalTokens: Int? = null,
  val estimatePriceTotalToken: Double? = null,
  val currency: String?
) {
  companion object {
    @JvmSynthetic
    operator fun invoke(usage: MessagesUsage, price: ModelsPricing?): OutputTokens {
      val estimateInputPrice =
        price?.let { usage.promptTokens.let { (it * price.input.price) / price.input.perTokens } }
      val estimateOutputPrice =
        price?.let {
          usage.completionTokens.let { (it * price.output.price) / price.output.perTokens }
        }
      val estimateTotalPrice = estimateInputPrice?.plus(estimateOutputPrice ?: 0.0)
      return OutputTokens(
        usage.promptTokens,
        estimateInputPrice,
        usage.completionTokens,
        estimateOutputPrice,
        usage.totalTokens,
        estimateTotalPrice,
        price?.currency
      )
    }
  }
}
