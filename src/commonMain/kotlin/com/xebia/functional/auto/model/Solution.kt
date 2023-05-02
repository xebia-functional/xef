package com.xebia.functional.auto.model

import com.xebia.functional.auto.agents.additionalContext
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Solution(
  val id: Int, val task: Task, val result: String, val accomplishesObjective: Boolean
)

suspend fun AIContext.solution(): Solution =
  ai(
    """|You are an AI who performs one task based on the following objective: 
       |${currentTask.objective}
       |
       |${additionalContext()}
       |If you can't solve the objective, use `false` for `accomplishesObjective` in the returned response. 
    """.trimMargin()
  )

internal fun AIContext.previousSolutionsContext(): String =
  if (previousSolutions.isEmpty()) ""
  else """|Take into account these previously attempted tasks:
          |${previousSolutions.joinToString("\n") { "- ${it.task.objective}\n\tresult:${it.result}\n\tAccomplishes Objective: ${it.accomplishesObjective}" }}.
          """.trimMargin()
