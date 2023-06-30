package com.xebia.functional.xef.auto.tot

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.prompt
import kotlinx.serialization.Serializable

@Serializable
data class Critique(val answer: String, val reasoning: String, val answerTrulyAccomplishesTheGoal: Boolean)

internal suspend fun <A> CoreAIScope.critique(memory: Memory<A>, currentSolution: Solution<A>): Critique {
  println("🕵️ Critiquing solution: ${truncateText(currentSolution.answer)}...")
  return prompt(
    """|
    |You are an expert advisor critiquing a solution.
    |
    |Previous history:
    |${renderHistory(memory)}
    |
    |You are given the following problem:
    |${memory.problem.description}
    |
    |You are given the following solution:
    |${currentSolution.answer}
    |
    |Instructions:
    |1. Provide a critique and determine if the answer truly accomplishes the goal.
    |
  """.trimMargin()
  )
}


