package com.xebia.functional.xef.reasoning.text.language

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class LanguageDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun identifyLanguage(text: String): LanguageIdentificationResult {
    logger.info { "🔍 Identifying language of text: ${text.length}" }
    return callModel<LanguageIdentificationResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in language identification that can identify the language of a given piece of text",
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
                "Identify the language of the `text`",
                "Your `RESPONSE` MUST be a `LanguageIdentificationResult` object, where the `language` is the identified language of the input text"
              ) + instructions
          )
      )
      .also { logger.info { "🔍 Language identification result: $it" } }
  }
}
