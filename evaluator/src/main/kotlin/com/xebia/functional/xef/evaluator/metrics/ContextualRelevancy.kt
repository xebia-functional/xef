package com.xebia.functional.xef.evaluator.metrics

import com.xebia.functional.xef.AI

enum class ContextualRelevancy : AI.PromptClassifier {
  high,
  mid,
  low;

  override fun template(input: String, output: String, context: String): String {
    return """|
      |You are an expert en evaluating whether the `output` is consistent with the given `context`.
      | <output>
      | $output
      | </output>
      | <context>
      | $context
      | </context>
      |Return one of the following:
      | - if the answer is high consistent: `high`
      | - if the answer is middle consistent: `mid`
      | - if the answer is low consistent: `low`
    """
      .trimMargin()
  }
}
