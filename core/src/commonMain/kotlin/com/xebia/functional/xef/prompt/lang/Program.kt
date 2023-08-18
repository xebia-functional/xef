package com.xebia.functional.xef.prompt.lang

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.serializer

class Program(
  val model: ChatWithFunctions,
  val conversation: Conversation,
  val description: String,
) {
  suspend inline operator fun <reified A, reified B> invoke(input: A): B {
    return model.prompt(
      prompt = Prompt { +user(input) },
      scope = conversation,
      serializer = serializer<B>(),
    )
  }
}
