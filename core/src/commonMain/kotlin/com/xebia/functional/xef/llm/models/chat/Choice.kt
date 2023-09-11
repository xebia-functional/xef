package com.xebia.functional.xef.llm.models.chat

import kotlinx.serialization.Serializable

@Serializable data class Choice(val message: Message?, val finishReason: String?, val index: Int?)
