package com.xebia.functional.auto.agents

import com.xebia.functional.auto.model.Solution
import com.xebia.functional.auto.model.Task

interface Agent<out A> {
    suspend fun context(task: Task, previousSolutions: List<Solution>): AgentContext<A>

    companion object
}
