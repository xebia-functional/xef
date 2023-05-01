package com.xebia.functional.auto.model

import com.xebia.functional.auto.agents.additionalContext
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int, val objective: String, val instructions: List<String>
)

suspend fun AIContext.refineTask(): Task = ai(
    """|You are an AI who refines a task based on the following objective: 
       |${currentTask.objective}
       |We have attempted to solve the with the current solution 
       |but we are not sure if it's successful. 
       |
       |${additionalContext()}
       |
       |Please refine the following task: ${currentTask.objective} to make it more specific, so that it can be solved.
    """.trimIndent()
)
