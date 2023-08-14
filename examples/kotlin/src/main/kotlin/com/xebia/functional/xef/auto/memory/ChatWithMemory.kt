package com.xebia.functional.xef.auto.memory

import com.xebia.functional.xef.auto.llm.openai.OpenAI

suspend fun main() {
  val model = OpenAI().DEFAULT_CHAT
  OpenAI.conversation {
    while (true) {
      println(">")
      val question = readLine() ?: break
      val answer = model.promptStreaming(question, this)
      answer.collect(::print)
      println()
    }
  }
}
