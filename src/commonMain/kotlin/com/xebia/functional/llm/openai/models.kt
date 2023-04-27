package com.xebia.functional.llm.openai

import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class EmbeddingModel(name: String) {
  TextEmbeddingAda002("text-embedding-ada-002")
}

data class RequestConfig(val model: EmbeddingModel, val user: User) {
  companion object {
    @JvmInline
    value class User(val id: String)
  }
}


@Serializable
data class CompletionChoice(val text: String, val index: Int, val finishReason: String)

@Serializable
data class CompletionRequest(
  val model: String,
  val user: String,
  val prompt: String? = null,
  val suffix: String? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null,
  val temperature: Double? = null,
  @SerialName("top_p") val topP: Double? = null,
  val n: Int? = null,
  val stream: Boolean? = null,
  val logprobs: Int? = null,
  val echo: Boolean? = null,
  val stop: List<String>? = null,
  @SerialName("presence_penalty") val presencePenalty: Double? = null,
  @SerialName("frequency_penalty") val frequencyPenalty: Double? = null,
  @SerialName("best_of") val bestOf: Int? = null,
  @SerialName("logit_bias") val logitBias: Map<String, Int>? = null,
)

@Serializable
data class EmbeddingRequest(val model: String, val input: List<String>, val user: String)

@Serializable
data class EmbeddingResult(
  val model: String,
  @SerialName("object") val `object`: String,
  val data: List<Embedding>,
  val usage: Usage
)

@Serializable
class Embedding(val `object`: String, val embedding: List<Float>, val index: Int)

@Serializable
data class Usage(
  @SerialName("prompt_tokens") val promptTokens: Long,
  @SerialName("completion_tokens") val completionTokens: Long,
  @SerialName("total_tokens") val totalTokens: Long
)
