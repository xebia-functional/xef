package com.xebia.functional.xef.evaluator.metrics

import com.xebia.functional.xef.AI

enum class AnswerAccuracy : AI.PromptClassifier {
  yes,
  no;

  override fun template(input: String, output: String, context: String): String {
    return """|
      |Return one of the following based on if the output is factual consistent or not with the given 
      | <input>
      | $input
      | </input>
      | <output>
      | $output
      | </output>
      | <context>
      | $context
      | </context>
      |Return one of the following:
      | - if `yes`: It's consistent
      | - if `no`: It's inconsistent
    """
      .trimMargin()
  }
}
