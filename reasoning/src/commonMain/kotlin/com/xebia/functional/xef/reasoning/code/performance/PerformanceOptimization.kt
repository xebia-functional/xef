package com.xebia.functional.xef.reasoning.code.performance

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import io.github.oshai.kotlinlogging.KotlinLogging

class PerformanceOptimization(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<PerformanceOptimizationResult> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<PerformanceOptimizationResult>> =
    mapOf(
      ToolMetadata(name = "optimizePerformance", description = "Optimize code performance") to
        ::optimizePerformance
    )

  override suspend fun handle(input: ToolOutput<Any?>): Tool.Out<PerformanceOptimizationResult> =
    optimizePerformance(input.toOutputString())

  suspend fun optimizePerformance(code: String): PerformanceOptimizationResult {
    logger.info { "🔍 Optimizing code performance" }
    return callModel<PerformanceOptimizationResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in performance optimization that can analyze code and provide recommendations to improve performance",
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
                "Optimize the given `code` for better performance",
                "Only optimize it if it makes sense to do so or the performance gain would be significant",
                "Don't micro optimize in cases where I/O is involved, I/O is usually the bottleneck",
                "Point out cases where I/O may be a bottleneck",
                "Your `RESPONSE` MUST be a list of `PerformanceOptimizationRecommendation` objects, where each object has the `category`, `description`, and `examples` of the performance optimization recommendation"
              ) + instructions
          )
      )
      
  }
}
