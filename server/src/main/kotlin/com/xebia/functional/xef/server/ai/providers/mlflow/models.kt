package com.xebia.functional.xef.server.ai.providers.mlflow

import io.ktor.util.date.*
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

@Serializable
data class XefChatRequest(
  val model: String,
  @Serializable(with = XefChatMessageSerializable::class) val messages: List<XefChatMessage>,
  val temperature: Double? = null,
  val n: Int? = null,
  val stop: List<String>? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null,
  val stream: Boolean? = false
)

@Serializable
data class XefEmbeddingsRequest(
  val model: String,
  @Serializable(with = StringArraySerializable::class) val input: List<String>,
  val encodingFormat: XefEncodingFormat? = null
)

@Serializable
enum class XefEncodingFormat {
  @SerialName("float") FLOAT,
  @SerialName("base64") BASE64
}

@Serializable data class XefChatMessage(val role: String, val content: String)

@Serializable
data class XefChatResponse(
  val id: String,
  @SerialName("object") val objectModel: String,
  val created: Long,
  val model: String,
  val choices: List<XefResponseChoice>,
  val usage: XefResponseUsage
)

@Serializable
data class XefResponseChoice(
  val index: Int,
  val message: XefResponseMessage,
  @SerialName("finish_reason") val finishReason: String?
)

@Serializable data class XefResponseMessage(val role: String, val content: String)

@Serializable
data class XefResponseUsage(
  @SerialName("prompt_tokens") val promptTokens: Int?,
  @SerialName("completion_tokens") val completionTokens: Int?,
  @SerialName("total_tokens") val totalTokens: Int?
)

@Serializable
data class XefEmbeddingResponse(
  @SerialName("object") val objectModel: String,
  val data: List<XefEmbedding>,
  val model: String,
  val usage: XefResponseUsage
)

@Serializable
data class XefEmbedding(
  @SerialName("object") val objectModel: String,
  val index: Int,
  val embedding: List<Float>
)

@Serializable
data class MLflowChatRequest(
  @Serializable(with = MLflowChatMessageSerializable::class) val messages: List<MLflowChatMessage>,
  val temperature: Double? = null,
  @SerialName("candidate_count") val candidateCount: Int? = null,
  val stop: List<String>? = null,
  @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
data class MLflowEmbeddingsRequest(
  @Serializable(with = StringArraySerializable::class) val text: List<String>
)

@Serializable
data class MLflowChatResponse(
  val candidates: List<MLflowChatCandidate>,
  val metadata: MLflowResponseMetadata
)

@Serializable
data class MLflowEmbeddingsResponse(
  val embeddings: List<List<Float>>,
  val metadata: MLflowResponseMetadata
)

@Serializable
data class MLflowChatCandidate(
  val message: MLflowChatMessage,
  val metadata: MLflowCandidateMetadata
)

@Serializable data class MLflowChatMessage(val role: String, val content: String)

@Serializable
data class MLflowCandidateMetadata(@SerialName("finish_reason") val finishReason: String?)

@Serializable
data class MLflowResponseMetadata(
  val model: String,
  @SerialName("route_type") val routeType: MLflowRouteType,
  @SerialName("input_tokens") val inputTokens: Int? = null,
  @SerialName("output_tokens") val outputTokens: Int? = null,
  @SerialName("total_tokens") val totalTokens: Int? = null
)

@Serializable
enum class MLflowRouteType {
  @SerialName("llm/v1/completions") COMPLETIONS,
  @SerialName("llm/v1/chat") CHAT,
  @SerialName("llm/v1/embeddings") EMBEDDINGS
}

private object XefChatMessageSerializable :
  JsonTransformingSerializer<List<XefChatMessage>>(ListSerializer(XefChatMessage.serializer())) {
  override fun transformDeserialize(element: JsonElement): JsonElement =
    if (element !is JsonArray) JsonArray(listOf(element)) else element
}

private object MLflowChatMessageSerializable :
  JsonTransformingSerializer<List<MLflowChatMessage>>(
    ListSerializer(MLflowChatMessage.serializer())
  ) {
  override fun transformSerialize(element: JsonElement): JsonElement =
    if (element is JsonArray) {
      val jsonArray = element.jsonArray
      if (jsonArray.size == 1) jsonArray[0] else jsonArray
    } else element
}

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

fun XefChatRequest.toMLflow(): MLflowChatRequest =
  MLflowChatRequest(
    messages.map { MLflowChatMessage(it.role, it.content) },
    temperature,
    n,
    stop,
    maxTokens
  )

fun XefEmbeddingsRequest.toMLflow(): MLflowEmbeddingsRequest = MLflowEmbeddingsRequest(input)

fun MLflowChatResponse.toXef(): XefChatResponse =
  XefChatResponse(
    UUID.generateUUID().toString(),
    "chat.completion",
    getTimeMillis(),
    metadata.model,
    candidates.mapIndexed { index, candidate ->
      XefResponseChoice(
        index,
        XefResponseMessage(candidate.message.role, candidate.message.content),
        candidate.metadata.finishReason
      )
    },
    metadata.toXef()
  )

fun MLflowEmbeddingsResponse.toXef(): XefEmbeddingResponse =
  XefEmbeddingResponse(
    "list",
    embeddings.mapIndexed { index, list -> XefEmbedding("embedding", index, list) },
    metadata.model,
    metadata.toXef().copy(completionTokens = null)
  )

private fun MLflowResponseMetadata.toXef(): XefResponseUsage =
  XefResponseUsage(inputTokens, outputTokens, totalTokens)
