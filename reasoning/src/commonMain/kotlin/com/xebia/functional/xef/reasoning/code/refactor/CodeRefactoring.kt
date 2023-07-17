package com.xebia.functional.xef.reasoning.code.refactor

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import io.github.oshai.kotlinlogging.KotlinLogging

class CodeRefactoring(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<RefactoringResult> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<RefactoringResult>> =
    mapOf(ToolMetadata(name = "refactorCode", description = "Refactor code") to ::refactorCode)

  suspend fun refactorCode(code: String): RefactoringResult {
    logger.info { "🔍 Refactoring code" }
    return callModel<RefactoringResult>(
        model,
        scope,
        ExpertSystem(
          system =
            "You are an expert in code refactoring that can improve the structure of a piece of code without changing its behavior",
          query =
            """|
                |Given the following code:
                |```code
                |${code}
                |```
            """
              .trimMargin(),
          instructions =
            listOf(
              "Refactor the `code` to improve its structure without changing its behavior",
              "Your `RESPONSE` MUST be the refactored code"
            ) + instructions
        )
      )
      .also { logger.info { "🔍 Refactoring result: $it" } }
  }
}
