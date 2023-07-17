package com.xebia.functional.xef.reasoning.code.api.usage.example

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class APIUsageExampleGenerationResult(val examples: List<APIUsageExample>) :
  Tool.Out<APIUsageExampleGenerationResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<APIUsageExampleGenerationResult> =
    ToolOutput(
      metadata,
      examples.map {
        """|
        |${it.apiName}
        |
        |${it.description}
        |
        |${it.codeSnippet}
        |
        |${it.exampleResult}
      """
          .trimMargin()
      },
      this
    )
}
