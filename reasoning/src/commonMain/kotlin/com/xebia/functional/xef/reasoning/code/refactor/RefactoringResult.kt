package com.xebia.functional.xef.reasoning.code.refactor

import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class RefactoringResult(
  val refactoredCode: String,
) : Tool.Out<RefactoringResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<RefactoringResult> {
    return ToolOutput(metadata, listOf(refactoredCode), this)
  }
}
