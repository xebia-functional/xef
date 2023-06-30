package com.xebia.functional.xef.llm.models.chat

data class Message(val role: String, val content: String?, val name: String? = Role.ASSISTANT.name)
