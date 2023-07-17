package com.xebia.functional.xef.reasoning.code.documentation

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput

data class CodeDocumentation(
  val title: String,
  val outline: String,
  val details: String,
  val examples: String
) : Tool.Out<CodeDocumentation> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<CodeDocumentation> =
    ToolOutput(
      metadata,
      listOf(
        """|
        |${title}
        |
        |${outline}
        |
        |${details}
        |
        |${examples}
      """
          .trimMargin()
      ),
      this
    )
}
