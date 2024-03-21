package com.xebia.functional.xef.evaluator.metrics

import com.xebia.functional.xef.AI

enum class AnswerAccuracy : AI.PromptClassifier {
  yes,
  no;

  override fun template(input: String, output: String, context: String): String {
    return """|
      |You are an expert en evaluating whether the `output` is consistent with the given `input` and `context`.
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
      | - if the answer it's consistent: `yes`
      | - if the answer it's not consistent: `no`
    """
      .trimMargin()
  }
}
