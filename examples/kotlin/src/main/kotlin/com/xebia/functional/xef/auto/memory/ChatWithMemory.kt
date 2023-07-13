package com.xebia.functional.xef.auto.memory

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.getOrThrow

suspend fun main() {
  val model = OpenAI.DEFAULT_CHAT
  ai {
    while (true) {
      println(">")
      val question = readLine() ?: break
      val answer = model.promptStreaming(question, context, conversationId)
      answer.collect(::print)
      println()
    }
  }.getOrThrow()
}
