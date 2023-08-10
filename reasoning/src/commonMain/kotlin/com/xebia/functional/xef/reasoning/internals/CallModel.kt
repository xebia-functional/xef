package com.xebia.functional.xef.reasoning.internals

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message

internal suspend fun callModel(
  model: Chat,
  scope: Conversation,
  prompt: List<Message>,
): String {
  return model
    .promptMessages(
      messages = prompt,
      scope = scope,
    )
    .firstOrNull()
    ?: error("No results found")
}
