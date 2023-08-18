package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
enum class ExecutionType {
  @Description(
    "The tool is executed in parallel with other tools and exclusively relies on the original user input"
  )
  Parallel,
  @Description(
    "The tool is executed after the previous tool has finished and depends on the previous tool's output" +
      "The previous tool to this tool in the execution plan is always a `Sequential` tool"
  )
  Sequential,
}
