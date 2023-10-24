package com.xebia.functional.xef.mlflow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoutesResponse(
    val routes: List<RouteDefinition>
)

@Serializable
data class RouteDefinition(
    val name: String,
    @SerialName("route_type")
    val routeType: String,
    val model: RouteModel,
    @SerialName("route_url")
    val routeUrl: String,
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
    @SerialName("candidate_count")
    val candidateCount: Int? = null,
    val stop: List<String>? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null
)

@Serializable
data class CandidateMetadata(
    @SerialName("finish_reason")
    val finishReason: String?
)

@Serializable
data class PromptCandidate(
    val text: String,
    val metadata: CandidateMetadata?
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
data class PromptResponse(
    val candidates: List<PromptCandidate>,
    val metadata: ResponseMetadata
)

@Serializable
data class ValidationDetail(
    val msg: String,
    val type: String
)

@Serializable
data class ValidationError(
    val detail: List<ValidationDetail>?
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
data class Chat(
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    @SerialName("candidate_count")
    val candidateCount: Int? = null,
    val stop: List<String>? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null
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
data class Embeddings(val text: List<String>)

@Serializable
data class EmbeddingsResponse(
    val embeddings: List<List<Float>>,
    val metadata: ResponseMetadata
)