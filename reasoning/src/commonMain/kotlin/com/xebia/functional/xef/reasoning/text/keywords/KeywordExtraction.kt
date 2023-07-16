package com.xebia.functional.xef.reasoning.text.keywords

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class KeywordExtraction(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun extractKeywords(text: String): KeywordExtractionResult {
    logger.info { "🔍 Extracting keywords from text: ${text.length}" }
    return callModel<KeywordExtractionResult>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert in keyword extraction that can identify and extract the key words or phrases from a piece of text",
        query = """|
                |Given the following text:
                |```text
                |${text}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Extract the key words or phrases from the `text`",
          "Your `RESPONSE` MUST be a list of keywords or phrases"
        )
      )
    ).also {
      logger.info { "🔍 Keyword extraction result: $it" }
    }
  }
}
