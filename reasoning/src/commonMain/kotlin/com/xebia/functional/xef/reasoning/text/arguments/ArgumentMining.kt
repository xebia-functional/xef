package com.xebia.functional.xef.reasoning.text.arguments

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class ArgumentMining(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun mineArguments(text: String): ArgumentMiningResult {
    logger.info { "🔍 Mining arguments from text: ${text.length}" }
    return callModel<ArgumentMiningResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in argument mining that can identify claims, supports, and objections in a piece of text",
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
                "Mine the arguments from the `text`",
                "Your `RESPONSE` MUST be a list of `Argument` objects, where each object has the `claim`, `supports`, and `objections`"
              ) + instructions
          ),
      )
      .also { logger.info { "🔍 Argument mining result: $it" } }
  }
}
