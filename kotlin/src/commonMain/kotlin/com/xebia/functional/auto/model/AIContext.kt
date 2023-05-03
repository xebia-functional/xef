package com.xebia.functional.auto.model

import com.xebia.functional.auto.agents.AgentContext

data class AIContext(
    val currentTask: Task, val previousSolutions: List<Solution>, val agentContexts: List<AgentContext<*>>
)
