package com.xebia.functional.xef.reasoning.text.coreference

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class CoreferenceResolution(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun resolveCoreferences(text: String): Coreferences {
    logger.info { "🔍 Resolving coreferences in text: $text" }
    return callModel<Coreferences>(
      model,
      scope,
      ExpertSystem(
        system = "You are an expert in coreference resolution that can identify the reference of each pronoun in a piece of text",
        query = """|
                |Given the following text:
                |```text
                |$text
                |```
            """.trimMargin(),
        instructions = listOf(
          "Identify the reference of each pronoun in the `text`",
          "Your `RESPONSE` MUST be a list of `Coreference` objects, where each object has the `pronoun` and its `reference`"
        )
      ),
    ).also {
      logger.info { "🔍 Coreference resolution result: $it" }
    }
  }
}
