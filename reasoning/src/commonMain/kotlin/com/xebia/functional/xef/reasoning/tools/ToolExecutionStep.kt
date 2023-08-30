package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.conversation.Description
import kotlinx.serialization.Serializable

@Serializable
data class ToolExecutionStep(
  @Description("Determines if the tool is executed in parallel or sequentially")
  val executionType: ExecutionType,
  @Description("The tool to execute") val tool: ToolMetadata,
  @Description("The reasoning for selecting this tool") val reasoning: String,
)
