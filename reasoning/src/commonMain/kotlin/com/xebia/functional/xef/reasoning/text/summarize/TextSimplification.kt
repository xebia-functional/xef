package com.xebia.functional.xef.reasoning.text.summarize

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class TextSimplification(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun simplifyText(text: String): SimplificationResult {
    logger.info { "🔍 Simplifying text: ${text.length}" }
    return callModel<SimplificationResult>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert in text simplification that can simplify a complex text into an easier to understand version",
        query = """|
                |Given the following text:
                |```text
                |${text}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Simplify the `text`",
          "Your `RESPONSE` MUST be a `SimplificationResult` object, where the `simplifiedText` is the simplified version of the input text"
        )
      )
    ).also {
      logger.info { "🔍 Text simplification result: $it" }
    }
  }
}
