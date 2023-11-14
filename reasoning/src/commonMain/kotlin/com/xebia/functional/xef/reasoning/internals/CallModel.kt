package com.xebia.functional.xef.reasoning.internals

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.promptMessages
import com.xebia.functional.xef.prompt.Prompt

internal suspend fun callModel(
  model: ChatApi,
  scope: Conversation,
  prompt: Prompt,
): String {
  return model
    .promptMessages(
      prompt = prompt,
      scope = scope,
    )
    .firstOrNull()
    ?: error("No results found")
}
