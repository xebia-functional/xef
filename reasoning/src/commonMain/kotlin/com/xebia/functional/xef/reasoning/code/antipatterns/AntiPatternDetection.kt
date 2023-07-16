package com.xebia.functional.xef.reasoning.code.antipatterns

import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import io.github.oshai.kotlinlogging.KotlinLogging

class AntiPatternDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun detectAntiPatterns(code: String): AntiPatternDetectionResult {
    logger.info { "🔍 Detecting anti-patterns in code" }
    return callModel<AntiPatternDetectionResult>(
      model,
      scope,
      ExpertSystem(
        system = "You are an expert in anti-pattern detection that can analyze code and identify common anti-patterns",
        query = """|
                |Given the following code:
                |```code
                |${code}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Detect anti-patterns in the given `code`",
          "Your `RESPONSE` MUST be a list of `AntiPattern` objects, where each object has the `name`, `description`, and `examples` of the detected anti-pattern"
        )
      ),
    ).also {
      logger.info { "🔍 Anti-pattern detection result: $it" }
    }
  }
}
