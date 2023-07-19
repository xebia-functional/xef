package com.xebia.functional.xef.reasoning.text.language

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class LanguageTranslation(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun translateText(text: String, targetLanguage: String): TranslationResult {
    logger.info { "🔍 Translating text: ${text.length} to $targetLanguage" }
    return callModel<TranslationResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in language translation that can translate a piece of text into a specified target language",
            query =
              """|
                |Given the following text:
                |```text
                |${text}
                |```
                |And the target language:
                |```language
                |${targetLanguage}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Translate the `text` into the `language`",
                "Your `RESPONSE` MUST be the translated text"
              ) + instructions
          )
      )
      
  }
}
