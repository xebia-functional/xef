package com.xebia.functional.xef.conversation.memory

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  OpenAI.conversation {
    val model = OpenAI().DEFAULT_CHAT
    while (true) {
      println(">")
      val question = readLine() ?: break
      val answer = model.promptStreaming(Prompt(question), this)
      answer.collect(::print)
      println()
    }
  }
}
