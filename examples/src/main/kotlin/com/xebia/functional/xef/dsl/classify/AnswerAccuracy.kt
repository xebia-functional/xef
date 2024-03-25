package com.xebia.functional.xef.dsl.classify

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.evaluator.metrics.AnswerAccuracy

/**
 * This is a simple example of how to use the `AI.classify` function to classify the accuracy of an
 * answer. In this case, it's using the `AnswerAccuracy` enum class to classify if the answer is
 * consistent or not.
 *
 * You can extend the `AI.PromptClassifier` interface to create your own classification. Override
 * the `template` function to define the prompt to be used in the classification.
 */
suspend fun main() {
  println(
    AI.classify<AnswerAccuracy>("Do I love Xef?", "I love Xef", "The answer responds the question")
  )
  println(
    AI.classify<AnswerAccuracy>(
      input = "Do I love Xef?",
      output = "I have three opened PRs",
      context = "The answer responds the question",
      model = CreateChatCompletionRequestModel._3_5_turbo_0125
    )
  )
}
