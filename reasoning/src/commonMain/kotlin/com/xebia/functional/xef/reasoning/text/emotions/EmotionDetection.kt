package com.xebia.functional.xef.reasoning.text.emotions

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class EmotionDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun detectEmotion(text: String): EmotionResult {
    logger.info { "🔍 Detecting emotion of text: ${text.length}" }
    return callModel<EmotionResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in emotion detection that can identify the predominant emotion in a piece of text",
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
                "Detect the emotion in the `text`",
                "Your `RESPONSE` MUST be one of the following: `HAPPINESS`, `SADNESS`, `ANGER`, `SURPRISE`, `DISGUST`, `FEAR`, `NEUTRAL`"
              ) + instructions
          ),
      )
      
  }
}
