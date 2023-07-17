package com.xebia.functional.xef.reasoning.code.tests

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class TestGenerationResult(val testCases: List<TestCase>) : Tool.Out<TestGenerationResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<TestGenerationResult> {
    return ToolOutput(
      metadata,
      testCases.map {
        """|
        |${it.type}
        |
        |${it.description}
        |
        |${it.code}
      """
          .trimMargin()
      },
      this
    )
  }
}
