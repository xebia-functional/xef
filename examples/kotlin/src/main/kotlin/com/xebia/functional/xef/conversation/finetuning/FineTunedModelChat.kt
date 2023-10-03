package com.xebia.functional.xef.conversation.finetuning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val spawnModelId =
    getenv("OPENAI_FINE_TUNED_MODEL_ID")
      ?: error("Please set the OPENAI_FINE_TUNED_MODEL_ID environment variable.")

  val OAI = OpenAI()
  val model = OAI.spawnModel(spawnModelId, OAI.GPT_3_5_TURBO)
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
