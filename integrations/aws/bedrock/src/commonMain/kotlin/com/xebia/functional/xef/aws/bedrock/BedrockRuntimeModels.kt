package com.xebia.functional.xef.aws.bedrock

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionResponse(
  val completion: String,
  @SerialName("stop_reason") val stopReason: String?,
  val stop: String?,
  @SerialName("amazon-bedrock-invocationMetrics") val invocationMetrics: InvocationMetrics? = null
) {
  @Serializable
  data class InvocationMetrics(
    val inputTokenCount: Int,
    val outputTokenCount: Int,
    val invocationLatency: Int,
    val firstByteLatency: Int,
  )
}
