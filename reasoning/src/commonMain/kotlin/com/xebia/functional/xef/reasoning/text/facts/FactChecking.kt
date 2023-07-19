package com.xebia.functional.xef.reasoning.text.facts

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class FactChecking(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun factCheck(statement: String, knownFacts: String): FactCheck {
    logger.info { "🔍 Fact-checking statement: $statement" }
    return callModel<FactCheck>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in fact-checking that can verify the truthfulness of a statement based on the provided known facts",
            query =
              """|
                |Given the following known facts:
                |```facts
                |${knownFacts}
                |```
                |And the following statement:
                |```statement
                |${statement}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Fact-check the `statement` based on the `facts`",
                "Your `RESPONSE` MUST be one of the following: `TRUE`, `FALSE`, `UNVERIFIABLE`"
              ) + instructions
          )
      )
      
  }
}
