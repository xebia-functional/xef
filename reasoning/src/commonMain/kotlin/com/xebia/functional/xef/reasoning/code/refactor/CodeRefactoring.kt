package com.xebia.functional.xef.reasoning.code.refactor

import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import io.github.oshai.kotlinlogging.KotlinLogging

class CodeRefactoring(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun refactorCode(code: String): RefactoringResult {
    logger.info { "🔍 Refactoring code" }
    return callModel<RefactoringResult>(
      model,
      scope,
      ExpertSystem(
        system = "You are an expert in code refactoring that can improve the structure of a piece of code without changing its behavior",
        query = """|
                |Given the following code:
                |```code
                |${code}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Refactor the `code` to improve its structure without changing its behavior",
          "Your `RESPONSE` MUST be the refactored code"
        )
      )
    ).also {
      logger.info { "🔍 Refactoring result: $it" }
    }
  }
}

