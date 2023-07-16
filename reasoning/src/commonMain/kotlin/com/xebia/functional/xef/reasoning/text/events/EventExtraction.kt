package com.xebia.functional.xef.reasoning.text.events

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class EventExtraction(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun extractEvents(text: String): EventExtractionResult {
    logger.info { "🔍 Extracting events from text: ${text.length}" }
    return callModel<EventExtractionResult>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert in event extraction that can identify who did what to whom, when, and where in a piece of text",
        query = """|
                |Given the following text:
                |```text
                |${text}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Extract the events from the `text`",
          "Your `RESPONSE` MUST be a list of `Event` objects, where each object has the `who`, `what`, `toWhom`, `when`, and `where`"
        )
      )
    ).also {
      logger.info { "🔍 Event extraction result: $it" }
    }
  }
}
