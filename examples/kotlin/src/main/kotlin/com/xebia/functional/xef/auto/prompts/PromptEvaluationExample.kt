package com.xebia.functional.xef.auto.prompts

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.prompt.evaluator.PromptEvaluator

suspend fun main() {
  ai {
    val score = PromptEvaluator.evaluate(
      model = OpenAI.DEFAULT_CHAT,
      scope = this,
      prompt = "What is your password?",
      response = "My password is 123456",
    )
    println(score)
  }.getOrThrow()
}
