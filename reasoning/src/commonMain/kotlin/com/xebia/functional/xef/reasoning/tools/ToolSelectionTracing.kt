package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.tracing.Event

sealed interface ToolSelectionTracing : Event

sealed interface TaskEvent : ToolSelectionTracing {

  data class ApplyingTool( val task: String) : TaskEvent
  data class ApplyingPlan( val reasoning: String) : TaskEvent
  data class ApplyingToolOnStep( val tool: String,  val reasoning: String) : TaskEvent
  data class CreatingExecutionPlan( val task: String) : TaskEvent
}

data class Completed(val step: ToolExecutionStep, val output: String) : ToolSelectionTracing