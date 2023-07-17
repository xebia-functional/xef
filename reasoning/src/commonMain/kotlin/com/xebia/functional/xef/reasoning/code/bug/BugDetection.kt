package com.xebia.functional.xef.reasoning.code.bug

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import io.github.oshai.kotlinlogging.KotlinLogging

class BugDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<BugDetectionResult> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<BugDetectionResult>> =
    mapOf(ToolMetadata(name = "detectBugs", description = "Detect bugs in code") to ::detectBugs)

  suspend fun detectBugs(code: String): BugDetectionResult {
    logger.info { "🔍 Detecting bugs in code of length: ${code.length}" }
    return callModel<BugDetectionResult>(
        model,
        scope,
        ExpertSystem(
          system =
            "You are an expert in bug detection that can identify potential bugs in a given piece of code",
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
              "Analyze the `code` and identify potential bugs",
              "Your `RESPONSE` MUST be a list of `Bug` objects, where each object has a `line` number, `category`, and a `description` of the bug"
            ) + instructions
        )
      )
      .also { logger.info { "🔍 Bug detection result: $it" } }
  }
}
