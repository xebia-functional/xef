package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class ToolSelectionResult(
  @Description(["The selected tool for the task"])
  val tool: Tool,
)
