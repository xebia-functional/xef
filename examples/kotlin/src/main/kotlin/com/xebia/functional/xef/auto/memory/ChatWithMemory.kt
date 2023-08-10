package com.xebia.functional.xef.auto.memory

import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.getOrThrow

suspend fun main() {
  val model = OpenAI.DEFAULT_CHAT
  conversation {
    while (true) {
      println(">")
      val question = readLine() ?: break
      val answer = model.promptStreaming(question, this)
      answer.collect(::print)
      println()
    }
  }.getOrThrow()
}
