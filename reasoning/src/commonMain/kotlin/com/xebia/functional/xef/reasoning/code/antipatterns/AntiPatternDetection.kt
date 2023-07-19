package com.xebia.functional.xef.reasoning.code.antipatterns

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import io.github.oshai.kotlinlogging.KotlinLogging

class AntiPatternDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<AntiPatternDetectionResult> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<AntiPatternDetectionResult>> =
    mapOf(
      ToolMetadata(name = "detectAntiPatterns", description = "Detect anti-patterns in code") to
        ::detectAntiPatterns
    )

  override suspend fun handle(input: ToolOutput<Any?>): Tool.Out<AntiPatternDetectionResult> =
    detectAntiPatterns(input.toOutputString())

  suspend fun detectAntiPatterns(code: String): AntiPatternDetectionResult {
    logger.info { "🔍 Detecting anti-patterns in code" }
    return callModel<AntiPatternDetectionResult>(
        model,
        scope,
        ExpertSystem(
          system =
            "You are an expert in anti-pattern detection that can analyze code and identify common anti-patterns",
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
              "Detect anti-patterns in the given `code`",
              "Your `RESPONSE` MUST be a list of `AntiPattern` objects, where each object has the `name`, `description`, and `examples` of the detected anti-pattern"
            ) + instructions
        ),
      )
      
  }
}
