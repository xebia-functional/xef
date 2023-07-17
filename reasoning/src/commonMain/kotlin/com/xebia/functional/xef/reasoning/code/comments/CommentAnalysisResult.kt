package com.xebia.functional.xef.reasoning.code.comments

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class CommentAnalysisResult(val analyses: List<CommentAnalysis>) :
  Tool.Out<CommentAnalysisResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<CommentAnalysisResult> =
    ToolOutput(
      metadata,
      analyses.map {
        """|
        |${it.comment}
        |
        |Quality: ${it.quality}
        |Completeness: ${it.completeness}
        |Usefulness: ${it.usefulness}
      """
          .trimMargin()
      },
      this
    )
}
