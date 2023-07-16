package com.xebia.functional.xef.reasoning.text.sentiments

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class SentimentAnalysis(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun analyzeSentiment(text: String): SentimentResult {
    logger.info { "🔍 Analyzing sentiment of text: ${text.length}" }
    return callModel<SentimentResult>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert sentiment analyzer that can classify a piece of text into positive, negative, or neutral sentiment",
        query = """|
                |Given the following text:
                |```text
                |${text}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Analyze the `text` and determine the sentiment",
          "Your `RESPONSE` MUST be one of the following: `POSITIVE`, `NEGATIVE`, `NEUTRAL`"
        )
      )
    ).also {
      logger.info { "🔍 Sentiment analysis result: $it" }
    }
  }
}
