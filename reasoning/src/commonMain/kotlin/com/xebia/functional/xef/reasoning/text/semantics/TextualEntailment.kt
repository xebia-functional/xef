package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class TextualEntailment(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun determineEntailment(premise: String, hypothesis: String): TextualEntailmentResult {
    logger.info {
      "🔍 Determining entailment between premise: $premise and hypothesis: $hypothesis"
    }
    return callModel<TextualEntailmentResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in textual entailment that can determine the entailment relation between a premise and a hypothesis",
            query =
              """|
                |Given the following premise:
                |```premise
                |${premise}
                |```
                |And the following hypothesis:
                |```hypothesis
                |${hypothesis}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Determine the entailment between the `premise` and the `hypothesis`",
                "Your `RESPONSE` MUST be a `TextualEntailmentResult` object, where the `entailment` is one of `ENTAILMENT`, `CONTRADICTION`, `NEUTRAL`"
              ) + instructions
          )
      )
      .also { logger.info { "🔍 Textual entailment result: $it" } }
  }
}
