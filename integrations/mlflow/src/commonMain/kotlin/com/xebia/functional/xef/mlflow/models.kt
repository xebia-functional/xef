package com.xebia.functional.xef.mlflow

import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.chat.create.CreateChatCompletionRequestStop
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestInput
import io.ktor.util.date.*
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonArray
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

@Serializable data class RoutesResponse(val routes: List<RouteDefinition>)

@Serializable
data class RouteDefinition(
  val name: String,
  @SerialName("route_type") val routeType: MLflowRouteType,
  val model: RouteModel,
  @SerialName("route_url") val routeUrl: String,
)

@Serializable
data class RouteModel(
  val name: String,
  val provider: String,
)

@Serializable
data class Prompt(
  val prompt: String,
  val temperature: Double? = null,
  @SerialName("candidate_count") val candidateCount: Int? = null,
  val stop: List<String>? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
data class MLflowCandidateMetadata(@SerialName("finish_reason") val finishReason: String?)

@Serializable data class PromptCandidate(val text: String, val metadata: MLflowCandidateMetadata?)

@Serializable
enum class MLflowRouteType {
  @SerialName("llm/v1/completions") COMPLETIONS,
  @SerialName("llm/v1/chat") CHAT,
  @SerialName("llm/v1/embeddings") EMBEDDINGS
}

@Serializable
data class MLflowResponseMetadata(
  val model: String,
  @SerialName("route_type") val routeType: MLflowRouteType,
  @SerialName("input_tokens") val inputTokens: Int? = null,
  @SerialName("output_tokens") val outputTokens: Int? = null,
  @SerialName("total_tokens") val totalTokens: Int? = null
)

@Serializable
data class MLflowPromptResponse(
  val candidates: List<PromptCandidate>,
  val metadata: MLflowResponseMetadata
)

@Serializable data class ValidationDetail(val msg: String, val type: String)

@Serializable data class ValidationError(val detail: List<ValidationDetail>?)

@Serializable
enum class MLflowChatRole {
  @SerialName("system") SYSTEM,
  @SerialName("user") USER,
  @SerialName("assistant") ASSISTANT
}

@Serializable data class MLflowChatMessage(val role: MLflowChatRole, val content: String)

@Serializable
data class MLflowChatRequest(
  @Serializable val messages: List<MLflowChatMessage>,
  val temperature: Double? = null,
  @SerialName("candidate_count") val candidateCount: Int? = null,
  val stop: List<String>? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
data class MLflowChatCandidate(
  val message: MLflowChatMessage,
  val metadata: MLflowCandidateMetadata
)

@Serializable
data class MLflowChatResponse(
  val candidates: List<MLflowChatCandidate>,
  val metadata: MLflowResponseMetadata
)

@Serializable
data class MLflowEmbeddingsRequest(
  @Serializable(with = StringArraySerializable::class) val text: List<String>
)

@Serializable
data class MLflowEmbeddingsResponse(
  val embeddings: List<List<Double>>,
  val metadata: MLflowResponseMetadata
)

private object StringArraySerializable :
  JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
  override fun transformDeserialize(element: JsonElement): JsonElement =
    if (element !is JsonArray) JsonArray(listOf(element)) else element

  override fun transformSerialize(element: JsonElement): JsonElement =
    if (element is JsonArray) {
      val jsonArray = element.jsonArray
      if (jsonArray.size == 1) jsonArray[0] else jsonArray
    } else element
}

fun CreateEmbeddingRequest.toMLflow(): MLflowEmbeddingsRequest =
  when (val i = input) {
    is CreateEmbeddingRequestInput.IntArrayArrayValue ->
      throw MLflowError.Unsupported("IntArrayArrayValue")
    is CreateEmbeddingRequestInput.IntArrayValue ->
      MLflowEmbeddingsRequest(i.v.map { it.toString() })
    is CreateEmbeddingRequestInput.StringArrayValue -> MLflowEmbeddingsRequest(i.v)
    is CreateEmbeddingRequestInput.StringValue -> MLflowEmbeddingsRequest(listOf(i.v))
  }

fun MLflowEmbeddingsResponse.toXef(): CreateEmbeddingResponse =
  CreateEmbeddingResponse(
    embeddings.mapIndexed { index, list -> Embedding(index, list, Embedding.Object.embedding) },
    metadata.model,
    CreateEmbeddingResponse.Object.list,
    metadata.toEmbeddingResponseUsage()
  )

fun CreateChatCompletionRequest.toMLflow(): MLflowChatRequest =
  MLflowChatRequest(
    messages.map { MLflowChatMessage(it.completionRole().toMLflow(), it.contentAsString()) },
    temperature,
    n,
    stop?.toMLflow(),
    maxTokens
  )

fun ChatCompletionRole.toMLflow(): MLflowChatRole =
  when (this) {
    ChatCompletionRole.system -> MLflowChatRole.SYSTEM
    ChatCompletionRole.user -> MLflowChatRole.USER
    ChatCompletionRole.assistant -> MLflowChatRole.ASSISTANT
    ChatCompletionRole.tool -> throw MLflowError.Unsupported("tool")
    ChatCompletionRole.function -> throw MLflowError.Unsupported("function")
  }

fun CreateChatCompletionRequestStop.toMLflow(): List<String> =
  when (this) {
    is CreateChatCompletionRequestStop.StringValue -> listOf(s)
    is CreateChatCompletionRequestStop.ArrayValue -> array
  }

fun MLflowChatResponse.toXef(): CreateChatCompletionResponse =
  CreateChatCompletionResponse(
    UUID.generateUUID().toString(),
    candidates.mapIndexed { index, candidate ->
      CreateChatCompletionResponseChoicesInner(
        candidate.metadata.finishReason?.toFinishReason()
          ?: throw MLflowError.Unsupported("FinishReason null"),
        index,
        ChatCompletionResponseMessage(
          candidate.message.content,
          candidate.message.role.toChatCompletionResponseMessageRole()
        )
      )
    },
    getTimeMillis().toInt(),
    metadata.model,
    CreateChatCompletionResponse.Object.chat_completion,
    null,
    metadata.toCompletionUsage()
  )

private fun MLflowChatRole.toChatCompletionResponseMessageRole():
  ChatCompletionResponseMessage.Role =
  when (this) {
    MLflowChatRole.ASSISTANT -> ChatCompletionResponseMessage.Role.assistant
    else -> throw MLflowError.Unsupported("MLflowChatRole $this")
  }

private fun String.toFinishReason(): CreateChatCompletionResponseChoicesInner.FinishReason =
  CreateChatCompletionResponseChoicesInner.FinishReason.entries.find { it.value == this }
    ?: throw MLflowError.Unsupported("FinishReason $this")

private fun MLflowResponseMetadata.toCompletionUsage(): CompletionUsage =
  CompletionUsage(
    completionTokens = outputTokens ?: 0,
    promptTokens = inputTokens ?: 0,
    totalTokens = totalTokens ?: 0
  )

private fun MLflowResponseMetadata.toEmbeddingResponseUsage(): CreateEmbeddingResponseUsage =
  CreateEmbeddingResponseUsage(promptTokens = inputTokens ?: 0, totalTokens = totalTokens ?: 0)

sealed class MLflowError @JvmOverloads constructor(message: String, cause: Throwable? = null) :
  RuntimeException(message, cause) {

  data class Unsupported(val msg: String) : MLflowError("Unsupported: $msg")
}
