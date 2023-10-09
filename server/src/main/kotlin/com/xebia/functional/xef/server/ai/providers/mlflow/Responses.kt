package com.xebia.functional.xef.server.ai.providers.mlflow

import io.ktor.util.date.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

@Serializable
data class OpenAIResponse(
    val id: String,
    @SerialName("object")
    val objectModel: String,
    val created: Long,
    val model: String,
    val choices: List<OpenAIResponseChoice>,
    val usage: OpenAIResponseUsage
)

@Serializable
data class OpenAIResponseChoice(
    val index: Int,
    val message: OpenAIResponseMessage,
    @SerialName("finish_reason")
    val finishReason: String?
)

@Serializable
data class OpenAIResponseMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIResponseUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int?,
    @SerialName("completion_tokens")
    val completionTokens: Int?,
    @SerialName("total_tokens")
    val totalTokens: Int?
)

@Serializable
data class OpenAIEmbeddingResponse(
    @SerialName("object")
    val objectModel: String,
    val data: List<OpenAIEmbedding>,
    val model: String,
    val usage: OpenAIResponseUsage
)

@Serializable
data class OpenAIEmbedding(
    @SerialName("object")
    val objectModel: String,
    val index: Int,
    val embedding: List<Float>
)

@Serializable
enum class RouteType {
    @SerialName("llm/v1/completions")
    COMPLETIONS,
    @SerialName("llm/v1/chat")
    CHAT,
    @SerialName("llm/v1/embeddings")
    EMBEDDINGS
}

@Serializable
data class CandidateMetadata(
    @SerialName("finish_reason")
    val finishReason: String?
)

@Serializable
data class ResponseMetadata(
    val model: String,
    @SerialName("route_type")
    val routeType: RouteType,
    @SerialName("input_tokens")
    val inputTokens: Int? = null,
    @SerialName("output_tokens")
    val outputTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)

@Serializable
enum class ChatRole {
    @SerialName("system")
    SYSTEM,
    @SerialName("user")
    USER,
    @SerialName("assistant")
    ASSISTANT
}

@Serializable
data class ChatMessage(
    val role: ChatRole,
    val content: String
)

@Serializable
data class ChatCandidate(
    val message: ChatMessage,
    val metadata: CandidateMetadata
)

@Serializable
data class ChatResponse(
    val candidates: List<ChatCandidate>,
    val metadata: ResponseMetadata
)

@Serializable
data class EmbeddingsResponse(
    val embeddings: List<List<Float>>,
    val metadata: ResponseMetadata
)

fun ResponseMetadata.toOpenAI(): OpenAIResponseUsage =
    OpenAIResponseUsage(inputTokens, outputTokens, totalTokens)

fun ChatResponse.toOpenAI(): OpenAIResponse =
    OpenAIResponse(
        UUID.generateUUID().toString(),
        "chat.completion",
        getTimeMillis(),
        metadata.model,
        candidates.mapIndexed { index, candidate ->
            OpenAIResponseChoice(
                index,
                OpenAIResponseMessage(candidate.message.role.name, candidate.message.content),
                candidate.metadata.finishReason
            )
        },
        metadata.toOpenAI()
    )

fun EmbeddingsResponse.toOpenAI(): OpenAIEmbeddingResponse =
    OpenAIEmbeddingResponse(
        "list",
        embeddings.mapIndexed { index, list ->
            OpenAIEmbedding("embedding", index, list)
        },
        metadata.model,
        metadata.toOpenAI().copy(completionTokens = null)
    )