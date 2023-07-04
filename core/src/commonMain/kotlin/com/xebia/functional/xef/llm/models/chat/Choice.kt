package com.xebia.functional.xef.llm.models.chat

data class Choice(val message: Message?, val finishReason: String?, val index: Int?)
