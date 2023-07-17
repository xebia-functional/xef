package com.xebia.functional.xef.reasoning.code.bug

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class BugDetectionResult(
  val bugs: List<Bug>,
) : Tool.Out<BugDetectionResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<BugDetectionResult> {
    return ToolOutput(
      metadata,
      bugs.map {
        """|
        |${it.category}
        |
        |${it.description}
        |
        |${it.line}
      """
          .trimMargin()
      },
      this
    )
  }
}
