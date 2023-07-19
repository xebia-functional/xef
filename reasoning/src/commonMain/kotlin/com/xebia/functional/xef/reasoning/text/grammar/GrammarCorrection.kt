package com.xebia.functional.xef.reasoning.text.grammar

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class GrammarCorrection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun correctGrammar(text: String): GrammarCorrectionResult {
    logger.info { "🔍 Correcting grammar of text: ${text.length}" }
    return callModel<GrammarCorrectionResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in grammar correction that can fix any grammatically unsound text and turn it into grammatically and sensically correct text",
            query =
              """|
                |Given the following text:
                |```text
                |${text}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Correct the grammar in the `text`",
                "Your `RESPONSE` MUST be the corrected text"
              ) + instructions
          )
      )
      
  }
}
