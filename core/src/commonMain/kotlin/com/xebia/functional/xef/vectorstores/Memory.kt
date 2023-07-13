package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.llm.models.chat.Message

data class Memory(val conversationId: ConversationId, val content: Message, val timestamp: Long)
