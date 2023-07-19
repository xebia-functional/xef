package com.xebia.functional.xef.reasoning.text.anonymization

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class DataAnonymization(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun anonymizeText(text: String): AnonymizationResult {
    logger.info { "🔍 Anonymizing text: ${text.length}" }
    return callModel<AnonymizationResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in data anonymization that can anonymize sensitive information in a given text",
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
                "Anonymize any sensitive information in the `text`",
                "Your `RESPONSE` MUST be an `AnonymizationResult` object with the `anonymizedText`"
              ) + instructions
          ),
      )
      
  }
}
