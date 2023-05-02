package com.xebia.functional.auto

import com.xebia.functional.auto.agents.Agent
import com.xebia.functional.auto.model.*

tailrec suspend fun solveObjective(
  task: Task,
  maxAttempts: Int = 5,
  previousSolutions: List<Solution> = emptyList(),
  agents: List<Agent<*>> = emptyList()
): Solution = if (maxAttempts <= 0) {
  Solution(-1, task, result = "Exceeded maximum attempts", accomplishesObjective = false)
} else {
  logger.debug { "Solving objective: ${task.objective} with agents $agents" }
  val contexts = agents.map { it.context(task, previousSolutions) }
  logger.debug { "Contexts: $contexts" }
  val ctx = AIContext(task, previousSolutions, contexts)
  val result: Solution = ctx.solution()
  logger.debug { "Solved: ${result.accomplishesObjective}" }
  if (result.accomplishesObjective) {
    logger.debug { "Solution accomplishes objective, proceeding to verification" }
    val verification: Verification = ctx.verify(result)
    if (verification.solvesTheProblemForReal) {
      logger.debug { "Solution verified: ${verification.solution.result} accepted: ${verification.solution.accomplishesObjective}" }
      result
    } else {
      val refinedTask: Task = ctx.refineTask()
      logger.debug { "Refined task: ${refinedTask.objective}" }
      solveObjective(refinedTask, maxAttempts - 1, previousSolutions + result)
    }
  } else {
    val refinedTask: Task = ctx.refineTask()
    logger.debug { "Refined task: ${refinedTask.objective}" }
    solveObjective(refinedTask, maxAttempts - 1, previousSolutions + result)
  }
}



