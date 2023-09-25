package com.xebia.functional.xef.conversation.finetuning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val model =
    OpenAI().GPT_3_5_TURBO.fineTuned("ft:gpt-3.5-turbo-0613:xebia-functional:ron-v1:81z5Sz3h")
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
