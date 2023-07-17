package com.xebia.functional.xef.reasoning.text.arguments

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class StanceDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun detectStance(text: String, target: String): StanceDetectionResult {
    logger.info { "🔍 Detecting stance of text: ${text.length} towards target: $target" }
    return callModel<StanceDetectionResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in stance detection that can identify the stance of a given piece of text towards a specific target",
            query =
              """|
                |Given the following text:
                |```text
                |${text}
                |```
                |And the following target:
                |```target
                |${target}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Identify the stance of the `text` towards the `target`",
                "Your `RESPONSE` MUST be a `StanceDetectionResult` object, where the `stance` is one of `FAVOR`, `AGAINST`, `NEUTRAL`"
              ) + instructions
          )
      )
      .also { logger.info { "🔍 Stance detection result: $it" } }
  }
}
