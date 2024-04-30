package com.xebia.functional.xef.evaluator.models

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.evaluator.metrics.ContextualRelevancy

@JvmInline
value class Markdown(val value: String) {
  companion object {
    fun <E> get(result: SuiteResults<E>, suiteName: String): Markdown where
    E : AI.PromptClassifier,
    E : Enum<E> {
      val content =
        """|
          |# Suite Results: $suiteName
          |- Description: ${result.description}
          |- Model: ${result.model}
          |- Metric: ${ContextualRelevancy::class.simpleName}
          |${
          result.items.joinToString("\n") { item ->
            """
              |#### Input: ${item.description}
              |${
              item.items.joinToString("\n") { outputResult ->
                """
                |- Description: ${outputResult.description}
                |- Context: ${outputResult.contextDescription}
                |- Output:
                |<blockquote>
                |${outputResult.output}
                |</blockquote>
                |- Usage:
                |<blockquote>
                |${outputResult.usage?.let { usage ->
                  """
                  |Completion Tokens: ${usage.completionTokens}
                  |Prompt Tokens: ${usage.promptTokens}
                  |Total Tokens: ${usage.totalTokens}
                  """.trimMargin()
                } ?: "No usage information available"}
                |</blockquote>
                |
                |Result: ${if (outputResult.success) "✅ Success" else "❌ Failure"} (${outputResult.result})
              """.trimMargin()
              }
            }
            """.trimMargin()
          }
        }
        """
          .trimMargin()
      return Markdown(content)
    }
  }
}
