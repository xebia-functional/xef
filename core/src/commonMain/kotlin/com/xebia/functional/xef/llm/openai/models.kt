package com.xebia.functional.xef.llm.openai

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class EmbeddingModel(val modelName: String) {
  TextEmbeddingAda002("text-embedding-ada-002")
}

data class RequestConfig(val model: EmbeddingModel, val user: User) {
  companion object {
    @JvmInline value class User(val id: String)
  }
}

@Serializable
data class CompletionChoice(
  val text: String,
  val index: Int,
  val logprobs: Int? = null,
  @SerialName("finish_reason") val finishReason: String
)

@Serializable
data class CompletionResult(
  val id: String,
  @SerialName("object") val `object`: String,
  val created: Long,
  val model: String,
  val choices: List<CompletionChoice>,
  val usage: Usage
)

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
  @SerialName("presence_penalty") val presencePenalty: Double = 0.0,
  @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
  @SerialName("best_of") val bestOf: Int = 1,
  @SerialName("logit_bias") val logitBias: Map<String, Int> = emptyMap(),
)

@Serializable
data class ChatCompletionRequest(
  val model: String,
  val messages: List<Message>,
  val temperature: Double = 0.0,
  @SerialName("top_p") val topP: Double = 1.0,
  val n: Int = 1,
  val stream: Boolean = false,
  val stop: List<String>? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null,
  @SerialName("presence_penalty") val presencePenalty: Double = 0.0,
  @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
  @SerialName("logit_bias") val logitBias: Map<String, Double>? = emptyMap(),
  val user: String?
)

@Serializable
data class ChatCompletionResponse(
  val id: String,
  val `object`: String,
  val created: Long,
  val model: String,
  val usage: Usage,
  val choices: List<Choice>
)

@Serializable
data class Choice(
  val message: Message,
  @SerialName("finish_reason") val finishReason: String,
  val index: Int
)

enum class Role {
  system,
  user,
  assistant
}

@Serializable
data class Message(val role: String, val content: String, val name: String? = Role.assistant.name)

@Serializable
data class EmbeddingRequest(val model: String, val input: List<String>, val user: String)

@Serializable
data class EmbeddingResult(
  val model: String,
  @SerialName("object") val `object`: String,
  val data: List<Embedding>,
  val usage: Usage
)

@Serializable class Embedding(val `object`: String, val embedding: List<Float>, val index: Int)

@Serializable
data class Usage(
  @SerialName("prompt_tokens") val promptTokens: Long,
  @SerialName("completion_tokens") val completionTokens: Long? = null,
  @SerialName("total_tokens") val totalTokens: Long
)

data class LLMModel(val name: String, val kind: Kind, val contextLength: Int) {
  enum class Kind {
    Completion,
    Chat
  }

  companion object {
    @JvmStatic val GPT_4 = LLMModel("gpt-4", Kind.Chat, 8192)

    @JvmStatic val GPT_4_0314 = LLMModel("gpt-4-0314", Kind.Chat, 8192)

    @JvmStatic val GPT_4_32K = LLMModel("gpt-4-32k", Kind.Chat, 32768)

    @JvmStatic val GPT_3_5_TURBO = LLMModel("gpt-3.5-turbo", Kind.Chat, 4096)

    @JvmStatic val GPT_3_5_TURBO_0301 = LLMModel("gpt-3.5-turbo-0301", Kind.Chat, 4096)

    @JvmStatic val TEXT_DAVINCI_003 = LLMModel("text-davinci-003", Kind.Completion, 4097)

    @JvmStatic val TEXT_DAVINCI_002 = LLMModel("text-davinci-002", Kind.Completion, 4097)

    @JvmStatic val TEXT_CURIE_001 = LLMModel("text-curie-001", Kind.Completion, 2049)

    @JvmStatic val TEXT_BABBAGE_001 = LLMModel("text-babbage-001", Kind.Completion, 2049)

    @JvmStatic val TEXT_ADA_001 = LLMModel("text-ada-001", Kind.Completion, 2049)
  }
}
