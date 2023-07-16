package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class WordSenseDisambiguation(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun disambiguateSenses(text: String): WordSenses {
    logger.info { "🔍 Disambiguating word senses in text: $text" }
    return callModel<WordSenses>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert in word sense disambiguation that can identify the meaning of each word in a piece of text based on its context",
        query = """|
                |Given the following text:
                |```text
                |$text
                |```
            """.trimMargin(),
        instructions = listOf(
          "Identify the sense of each word in the `text`",
          "Your `RESPONSE` MUST be a list of `WordSense` objects, where each object has the `word` and its `sense`"
        )
      )
    ).also {
      logger.info { "🔍 Word sense disambiguation result: $it" }
    }
  }
}

