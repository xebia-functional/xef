package com.xebia.functional.xef.evaluator.models

import com.xebia.functional.xef.PromptClassifier
import com.xebia.functional.xef.evaluator.metrics.ContextualRelevancy

@JvmInline
value class Markdown(val value: String) {
  companion object {
    fun <E> get(result: SuiteResults<E>, suiteName: String): Markdown where
    E : PromptClassifier,
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
                  |Prompt Tokens: ${usage.promptTokens} ${usage.estimatePricePerToken?.let { "(~ ${it.to2DecimalsString()} ${usage.currency ?: ""})" } ?: "" }
                  |Completion Tokens: ${usage.completionTokens} ${usage.estimatePriceCompletionToken?.let { "(~ ${it.to2DecimalsString()} ${usage.currency ?: ""})" } ?: "" }
                  |Total Tokens: ${usage.totalTokens}
                  |Total Price: ${usage.estimatePriceTotalToken?.let { "${it.to2DecimalsString()} ${usage.currency ?: ""}" } ?: "Unknown"}
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

    private fun Double.to2DecimalsString() = String.format("%.6f", this)
  }
}
