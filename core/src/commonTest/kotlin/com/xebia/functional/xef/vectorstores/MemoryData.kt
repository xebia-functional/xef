package com.xebia.functional.xef.vectorstores

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

val defaultConversationId = ConversationId("default-id")

val messages1 = listOf(
    Memory(defaultConversationId, Message(Role.USER, "Who is the best player in the world?", "USER"), 0),
    Memory(defaultConversationId, Message(Role.ASSISTANT, "Magico Gonzalez", "ASSISTANT"), 0),
)

val messages2 = listOf(
    Memory(defaultConversationId, Message(Role.USER, "Which is the most beautiful city in the world?", "USER"), 0),
    Memory(defaultConversationId, Message(Role.ASSISTANT, "More than a city better an area, La Bahia de Cadiz", "ASSISTANT"), 0),
)
