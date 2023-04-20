package llm.openai

import kotlin.js.JsName
import kotlinx.serialization.Serializable

@Serializable
data class CompletionChoice(val text: String, val index: Int, val finishReason: String)

@Serializable
data class CompletionRequest(
  val model: String,
  val user: String,
  val prompt: String? = null,
  val suffix: String? = null,
  @JsName("max_tokens") val maxTokens: Int? = null,
  val temperature: Double? = null,
  @JsName("top_p") val topP: Double? = null,
  val n: Int? = null,
  val stream: Boolean? = null,
  val logprobs: Int? = null,
  val echo: Boolean? = null,
  val stop: List<String>? = null,
  @JsName("presence_penalty") val presencePenalty: Double? = null,
  @JsName("frequency_penalty") val frequencyPenalty: Double? = null,
  @JsName("best_of") val bestOf: Int? = null,
  @JsName("logit_bias") val logitBias: Map<String, Int>? = null,
)

@Serializable
data class EmbeddingRequest(val model: String, val input: List<String>, val user: String)

@Serializable
data class EmbeddingResult(
  val model: String,
  @JsName("object") val `object`: String,
  val data: List<Embedding>,
  val usage: Usage
)

@Serializable
class Embedding(val `object`: String, val embedding: List<Double>, val index: Int)

@Serializable
data class Usage(
  @JsName("prompt_tokens") val promptTokens: Long,
  @JsName("completion_tokens") val completionTokens: Long,
  @JsName("total_tokens") val totalTokens: Long
)
