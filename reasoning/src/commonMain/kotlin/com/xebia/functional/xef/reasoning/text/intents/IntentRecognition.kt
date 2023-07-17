package com.xebia.functional.xef.reasoning.text.intents

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class IntentRecognition(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun recognizeIntent(userInput: String): IntentRecognitionResult {
    logger.info { "🔍 Recognizing intent: ${userInput.length}" }
    return callModel<IntentRecognitionResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in intent recognition that can identify the intent behind a user's input",
            query =
              """|
                    |Given the following user input:
                    |```text
                    |${userInput}
                    |```
                """
                .trimMargin(),
            instructions =
              listOf(
                "Recognize the intent of the user input",
                "Your `RESPONSE` MUST be an `IntentRecognitionResult` object with the `intent`"
              ) + instructions
          )
      )
      .also { logger.info { "🔍 Intent recognition result: $it" } }
  }
}
