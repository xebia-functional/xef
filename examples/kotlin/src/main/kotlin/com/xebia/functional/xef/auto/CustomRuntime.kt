package com.xebia.functional.xef.auto

import com.xebia.functional.xef.llm.openai.simpleMockAIClient

suspend fun main() {
  val program = ai {
    val love: List<String> = promptMessage("tell me you like me with just emojis")
    println(love)
  }
  program.getOrElse(customRuntime()) { println(it) }
}

private fun <A> customRuntime(): AIRuntime<A> =
  AIRuntime { block ->
    MockAIScope(
     simpleMockAIClient { it },
      block
    ) { throw it }
  }
