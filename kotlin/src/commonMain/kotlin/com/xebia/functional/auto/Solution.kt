package com.xebia.functional.auto

import kotlinx.serialization.Serializable

@Serializable
data class Solution(val objective: String, val result: String, val objectiveAccomplished: Boolean) {
  val prompt =
    """
        |Objective: $objective
        |Result: $result
        |Accomplishes objective: $objectiveAccomplished
    """
      .trimMargin()
}

@Serializable
data class AdditionalTasks(val objective: String, val tasks: List<String>) {
  val prompt =
    """
        |Objective: $objective
        |Tasks: ${tasks.joinToString("\n")}
    """
      .trimMargin()
}

@Serializable
data class Reassurance(
  val objective: String,
  val objectiveAccomplished: Boolean,
  val tasksWouldHelpAccomplishObjective: Map<String, Boolean>
)
