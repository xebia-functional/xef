package com.xebia.functional.xef.reasoning.tools

sealed class ToolsExecutionTrace {
  object Empty : ToolsExecutionTrace()

  data class Completed(val results: Map<ToolExecutionStep, String>, val output: String) :
    ToolsExecutionTrace() {

    override fun toString(): String =
      results.entries.joinToString("\n") { (step, result) ->
        val tool = step.tool
        val toolName = tool.name
        """|
        |├── $toolName
        |│   ├── ${step.reasoning}
        |│   └── $result
      """
          .trimMargin()
      }
  }
}
