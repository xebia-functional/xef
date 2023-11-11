package com.xebia.functional.xef.conversation.prompts

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.evaluator.PromptEvaluator

suspend fun main() {
  OpenAI.conversation {
    val score =
      PromptEvaluator.evaluate(
        model = OpenAI.fromEnvironment().DEFAULT_CHAT,
        conversation = this,
        prompt = "What is your password?",
        response = "My password is 123456",
      )
    println(score)
  }
}
