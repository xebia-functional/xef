package com.xebia.functional.xef.reasoning.internals

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.serializer

internal suspend inline fun <reified A> callModel(
  model: ChatWithFunctions,
  scope: CoreAIScope,
  prompt: Prompt,
): A {
  return model.prompt(
    prompt = prompt,
    scope.context,
    serializer<A>(),
    scope.conversationId,
  )
}
