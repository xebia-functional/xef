package com.xebia.functional.xef.conversation.finetuning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val OAI = OpenAI()
  val model = OAI.spawnModel("ft:gpt-3.5-turbo-0613:xebia-functional:ron-v2:830qZBwQ", OAI.GPT_3_5_TURBO)
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