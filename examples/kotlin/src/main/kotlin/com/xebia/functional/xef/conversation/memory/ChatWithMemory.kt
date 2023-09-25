package com.xebia.functional.xef.conversation.memory

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val model = OpenAI().DEFAULT_CHAT
  OpenAI.conversation {
    while (true) {
      print("> ")
      val question = readlnOrNull() ?: break
      val answer = model.promptStreaming(Prompt(question), this)
      answer.collect(::print)
      println()
    }
  }
}
