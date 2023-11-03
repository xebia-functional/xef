package com.xebia.functional.xef.server.http.client.mlflow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonArray

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

@Serializable
data class MLflowEmbeddingsRequest(
    @Serializable(with = StringArraySerializable::class) val text: List<String>
)

@Serializable
data class MLflowEmbeddingsResponse(
    val embeddings: List<List<Float>>,
    val metadata: MLflowResponseMetadata
)

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

@Serializable data class XefResponseMessage(val role: String, val content: String)

@Serializable
data class XefResponseUsage(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null
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

fun XefEmbeddingsRequest.toMLflow(): MLflowEmbeddingsRequest = MLflowEmbeddingsRequest(input)

fun MLflowEmbeddingsResponse.toXef(): XefEmbeddingResponse =
    XefEmbeddingResponse(
        "list",
        embeddings.mapIndexed { index, list -> XefEmbedding("embedding", index, list) },
        metadata.model,
        metadata.toXef().copy(completionTokens = null)
    )

private fun MLflowResponseMetadata.toXef(): XefResponseUsage =
    XefResponseUsage(inputTokens, outputTokens, totalTokens)