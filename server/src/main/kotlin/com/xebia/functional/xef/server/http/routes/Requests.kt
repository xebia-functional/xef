package com.xebia.functional.xef.server.http.routes

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: ChatRole, val content: String? = null, val name: String? = null
)

@JvmInline
@Serializable
value class ChatRole(val role: String) {
    public companion object {
        public val System: ChatRole = ChatRole("system")
        public val User: ChatRole = ChatRole("user")
        public val Assistant: ChatRole = ChatRole("assistant")
        public val Function: ChatRole = ChatRole("function")
    }
}


@Serializable
@Resource("/chat/completions")
data class PromptMessageRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 1.0,
    @SerialName("top_p") val topP: Double = 1.0,
    val n: Int = 1,
    val stop: List<String> = emptyList(),
    @SerialName("max_tokens") val maxTokens: Int = 16,
    @SerialName("presence_penalty") val presencePenalty: Double = 0.0,
    @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
    @SerialName("logit_bias") val logitBias: Map<String, Int> = emptyMap(),
    val user: String = "xef"
)
