package com.xebia.functional.xef.reasoning.internals

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message

internal suspend fun callModel(
  model: Chat,
  scope: CoreAIScope,
  prompt: List<Message>,
): String {
  return model
    .promptMessages(
      messages = prompt,
      context = scope.context,
      conversationId = scope.conversationId,
    )
    .firstOrNull()
    ?: error("No results found")
}
