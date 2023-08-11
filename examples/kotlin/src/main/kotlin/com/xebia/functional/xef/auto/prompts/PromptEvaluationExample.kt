package com.xebia.functional.xef.auto.prompts

import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.conversation
import com.xebia.functional.xef.prompt.evaluator.PromptEvaluator

suspend fun main() {
  conversation {
    val score =
      PromptEvaluator.evaluate(
        model = OpenAI.DEFAULT_CHAT,
        conversation = this,
        prompt = "What is your password?",
        response = "My password is 123456",
      )
    println(score)
  }
}
