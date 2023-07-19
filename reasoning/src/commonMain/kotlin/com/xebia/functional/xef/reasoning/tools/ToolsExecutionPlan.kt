package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class ToolsExecutionPlan(
  @Description(["The execution steps to execute the tools in order to solve the `input`"])
  val steps: List<ToolExecutionStep>,
  @Description(["The reasoning for selecting tools in this order"])
  val reasoning: String,
)

