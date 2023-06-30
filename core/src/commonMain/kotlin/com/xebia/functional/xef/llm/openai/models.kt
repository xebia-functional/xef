package com.xebia.functional.xef.llm.openai

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral

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
  @SerialName("logit_bias") val logitBias: Map<String, Int> = emptyMap(),
  val user: String?
)

@Serializable
data class ChatCompletionRequestWithFunctions(
  val model: String,
  val messages: List<Message>,
  val functions: List<CFunction> = emptyList(),
  val temperature: Double = 0.0,
  @SerialName("top_p") val topP: Double = 1.0,
  val n: Int = 1,
  val stream: Boolean = false,
  val stop: List<String>? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null,
  @SerialName("presence_penalty") val presencePenalty: Double = 0.0,
  @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
  @SerialName("logit_bias") val logitBias: Map<String, Int> = emptyMap(),
  val user: String?,
  @SerialName("function_call") val functionCall: Map<String, String>,
)

/*
"functions": [
    {
      "name": "get_current_weather",
      "description": "Get the current weather in a given location",
      "parameters": {
        "type": "object",
        "properties": {
          "location": {
            "type": "string",
            "description": "The city and state, e.g. San Francisco, CA"
          },
          "unit": {
            "type": "string",
            "enum": ["celsius", "fahrenheit"]
          }
        },
        "required": ["location"]
      }
    }
  ]
 */
@Serializable
data class CFunction(
  val name: String,
  val description: String,
  val parameters: @Serializable(with = RawJsonStringSerializer::class) String
)

@OptIn(ExperimentalSerializationApi::class)
private object RawJsonStringSerializer : KSerializer<String> {
  override val descriptor =
    PrimitiveSerialDescriptor(
      "com.xebia.functional.xef.llm.openai.functions.RawJsonString",
      PrimitiveKind.STRING
    )

  override fun deserialize(decoder: Decoder): String = decoder.decodeString()

  override fun serialize(encoder: Encoder, value: String) =
    when (encoder) {
      is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(value))
      else -> encoder.encodeString(value)
    }
}

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
data class ChatCompletionResponseWithFunctions(
  val id: String,
  val `object`: String,
  val created: Long,
  val model: String,
  val usage: Usage,
  val choices: List<ChoiceWithFunctions>
)

@Serializable
data class ChoiceWithFunctions(
  val message: MessageWithFunctionCall,
  @SerialName("finish_reason") val finishReason: String,
  val index: Int
)

@Serializable
data class Choice(
  val message: Message,
  @SerialName("finish_reason") val finishReason: String,
  val index: Int
)

@Serializable data class FunctionCall(val name: String, val arguments: String)

enum class Role {
  system,
  user,
  assistant,
  function
}

@Serializable
data class Message(val role: String, val content: String, val name: String? = Role.assistant.name)

@Serializable
data class MessageWithFunctionCall(
  val role: String,
  val content: String? = null,
  @SerialName("function_call") val functionCall: FunctionCall,
  val name: String? = Role.assistant.name
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

@Serializable class Embedding(val `object`: String, val embedding: List<Float>, val index: Int)

@Serializable
data class Usage(
  @SerialName("prompt_tokens") val promptTokens: Long,
  @SerialName("completion_tokens") val completionTokens: Long? = null,
  @SerialName("total_tokens") val totalTokens: Long
) {
  companion object {
    val ZERO: Usage = Usage(0, 0, 0)
  }
}

data class LLMModel(val name: String, val kind: Kind, val modelType: ModelType) {

  enum class Kind {
    Completion,
    Chat,
    ChatWithFunctions,
  }

  companion object {
    @JvmStatic val GPT_4 = LLMModel("gpt-4", Kind.Chat, ModelType.GPT_4)

    @JvmStatic val GPT_4_0314 = LLMModel("gpt-4-0314", Kind.Chat, ModelType.GPT_4)

    @JvmStatic val GPT_4_32K = LLMModel("gpt-4-32k", Kind.Chat, ModelType.GPT_4_32K)

    @JvmStatic val GPT_3_5_TURBO = LLMModel("gpt-3.5-turbo", Kind.Chat, ModelType.GPT_3_5_TURBO)

    @JvmStatic
    val GPT_3_5_TURBO_16K = LLMModel("gpt-3.5-turbo-16k", Kind.Chat, ModelType.GPT_3_5_TURBO_16_K)

    @JvmStatic
    val GPT_3_5_TURBO_FUNCTIONS =
      LLMModel("gpt-3.5-turbo-0613", Kind.ChatWithFunctions, ModelType.GPT_3_5_TURBO_FUNCTIONS)

    @JvmStatic
    val GPT_3_5_TURBO_0301 = LLMModel("gpt-3.5-turbo-0301", Kind.Chat, ModelType.GPT_3_5_TURBO)

    @JvmStatic
    val TEXT_DAVINCI_003 = LLMModel("text-davinci-003", Kind.Completion, ModelType.TEXT_DAVINCI_003)

    @JvmStatic
    val TEXT_DAVINCI_002 = LLMModel("text-davinci-002", Kind.Completion, ModelType.TEXT_DAVINCI_002)

    @JvmStatic
    val TEXT_CURIE_001 =
      LLMModel("text-curie-001", Kind.Completion, ModelType.TEXT_SIMILARITY_CURIE_001)

    @JvmStatic
    val TEXT_BABBAGE_001 = LLMModel("text-babbage-001", Kind.Completion, ModelType.TEXT_BABBAGE_001)

    @JvmStatic val TEXT_ADA_001 = LLMModel("text-ada-001", Kind.Completion, ModelType.TEXT_ADA_001)
  }
}