package com.xebia.functional.xef.prompt.lang

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.ChatWithFunctions
import kotlinx.serialization.serializer

class Program(
  val model: ChatWithFunctions,
  val conversation: Conversation,
  val description: String,
) {
  suspend inline operator fun <reified A, reified B> invoke(input: A): B {
    return model.prompt(
      input = input,
      scope = conversation,
      inputSerializer = serializer<A>(),
      outputSerializer = serializer<B>(),
    )
  }
}
