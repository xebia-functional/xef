package com.xebia.functional.auto.model

import com.xebia.functional.auto.agents.additionalContext
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Verification(val id: Int, val solution: Solution, val solvesTheProblemForReal: Boolean)

suspend fun AIContext.verify(result: Solution): Verification = ai(
    """|You are an AI who verifies an objective has been accomplished for one task based on the following objective: 
       |We have attempted to solve the following task objective: 
       |${currentTask.objective} 
       |We came up with the current solution, but we are not sure if it's successful. 
       |Please verify that the following solution: ${result.result} accomplishes the objective. 
       |Be very critical until you are sure that the solution solves the objective.
       |
       |${additionalContext()}
       |
    """.trimIndent()
)
