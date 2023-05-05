package com.xebia.functional.prompt.chat

import kotlinx.serialization.Serializable

enum class Type {
    human, ai, system, chat
}

@Serializable
sealed class Message(open val content: String, val type: String)

data class HumanMessage(override val content: String) : Message(content, Type.human.name)
data class AIMessage(override val content: String) : Message(content, Type.ai.name)
data class SystemMessage(override val content: String) : Message(content, Type.system.name)
data class ChatMessage(override val content: String, val role: String) : Message(content, Type.chat.name)
