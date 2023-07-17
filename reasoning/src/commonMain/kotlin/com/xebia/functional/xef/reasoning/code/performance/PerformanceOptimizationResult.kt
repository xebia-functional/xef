package com.xebia.functional.xef.reasoning.code.performance

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class PerformanceOptimizationResult(
  val recommendations: List<PerformanceOptimizationRecommendation>
) : Tool.Out<PerformanceOptimizationResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<PerformanceOptimizationResult> {
    return ToolOutput(
      metadata,
      recommendations.map {
        """|
        |${it.category}
        |
        |${it.description}
        |
        |${it.examples.joinToString("\n")}
      """
          .trimMargin()
      },
      this
    )
  }
}
