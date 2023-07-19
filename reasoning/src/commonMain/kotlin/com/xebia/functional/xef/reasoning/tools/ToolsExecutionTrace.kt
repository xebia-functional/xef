package com.xebia.functional.xef.reasoning.tools

import kotlin.jvm.JvmField

data class ToolsExecutionTrace(
  val results: Map<ToolExecutionStep, Tool.Out<*>>,
  val output: Tool.Out<*>
) {

  override fun toString(): String =
    results.entries.joinToString("\n") { (step, result) ->
      val tool = step.tool
      val toolName = tool.name
      val toolOutput = result.toolOutput(tool)
      """|
        |├── $toolName
        |│   ├── ${step.reasoning}
        |│   └───────────────
        |│   ${toolOutput.toOutputString()}
        |│   
      """.trimMargin()
    }

  companion object {
    @JvmField
    val EMPTY = ToolsExecutionTrace(emptyMap(), Tool.Out.empty<Any?>())
  }
}
