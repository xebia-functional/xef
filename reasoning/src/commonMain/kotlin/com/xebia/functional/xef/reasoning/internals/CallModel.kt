package com.xebia.functional.xef.reasoning.internals

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt

internal suspend fun callModel(
  model: Chat,
  scope: CoreAIScope,
  prompt: Prompt,
): String {
  return model.promptMessage(
    question = prompt.message,
    context = scope.context,
    conversationId = scope.conversationId,
  )
}
