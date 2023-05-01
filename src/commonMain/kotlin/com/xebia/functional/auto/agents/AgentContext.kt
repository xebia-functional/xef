package com.xebia.functional.auto.agents

import com.xebia.functional.auto.model.AIContext
import com.xebia.functional.auto.model.Solution
import com.xebia.functional.auto.model.Task
import com.xebia.functional.auto.model.previousSolutionsContext

data class AgentContext<out A>(
    val task: Task, val previousSolutions: List<Solution>, val value: A, val output: String
)

internal fun AIContext.additionalContext(): String =
    if (agentContexts.isEmpty()) previousSolutionsContext() else
    """|${previousSolutionsContext()}
       |
       |Additional context provided by other agents for this context is shown below and delimited
       |by a line of dashes
       |-----------------------------------------------------------------------
       |${agentContexts.joinToString("\n") { "- ${it.task.objective}\n\tresult:${it.output}\n\t" }}.
       |-----------------------------------------------------------------------
       |""".trimMargin()
