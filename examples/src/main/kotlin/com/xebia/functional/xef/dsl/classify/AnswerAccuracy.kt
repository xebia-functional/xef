package com.xebia.functional.xef.dsl.classify

import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.evaluator.metrics.AnswerAccuracy

suspend fun main() {
  println(
    AI.classify<AnswerAccuracy>("Do I love Xef?", "I love Xef", "The answer responds the question")
  )
  println(
    AI.classify<AnswerAccuracy>(
      input = "Do I love Xef?",
      output = "I have three opened PRs",
      context = "The answer responds the question",
      model = CreateChatCompletionRequestModel.gpt_3_5_turbo_0125
    )
  )
}
