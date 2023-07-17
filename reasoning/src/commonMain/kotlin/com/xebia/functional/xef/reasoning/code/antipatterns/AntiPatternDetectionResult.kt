package com.xebia.functional.xef.reasoning.code.antipatterns

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class AntiPatternDetectionResult(
  val detectedAntiPatterns: List<AntiPattern>,
) : Tool.Out<AntiPatternDetectionResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<AntiPatternDetectionResult> =
    ToolOutput(
      metadata,
      detectedAntiPatterns.map {
        """|
        |${it.name}
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
