package com.xebia.functional.xef.auto.tot

import com.xebia.functional.xef.auto.Conversation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class Problem(val description: String)

@Serializable
data class Memory<out A>(val problem: Problem, val history: List<Solution<A>>)

// Recursive function to solve the problem using the ToT framework
suspend inline fun <reified A> Conversation.solve(problem: Problem, maxRounds: Int): Solution<A> {
  val initialMemory = Memory<A>(problem, emptyList())
  return solveRec(problem, serializer<A>(), maxRounds, initialMemory)
}

@PublishedApi
internal tailrec suspend fun <A> Conversation.solveRec(problem: Problem, serializer: KSerializer<A>, remainingRounds: Int, memory: Memory<A>): Solution<A> =
  if (remainingRounds == 0) {
    println("❌ Maximum rounds reached. Unable to find a solution.")
    Solution<A>("", false, "No response", null)
  } else {
    println(
      "🌱 Solving problem: ${
        truncateText(
          memory.problem.description,
          100
        )
      } (Remaining rounds: $remainingRounds)..."
    )
    val controlSignal = controlSignal(memory)
    val solutionSerializer = Solution.serializer(serializer)
    val response = solution(solutionSerializer, memory, controlSignal)
    val result = checkSolution(response)
    val updatedMemory = Memory(problem, memory.history + result)
    if (result.isValid) {
      println("✅ Solution found: ${truncateText(result.answer)}!")
      val critique = critique(memory, result)
      if (!critique.answerTrulyAccomplishesTheGoal) {
        println("❌ Solution does not accomplish the goal: ${truncateText(result.answer)}!")
        println("⏪ Backtracking...")
        solveRec(problem, serializer, remainingRounds - 1, updatedMemory)
      } else result
    } else {
      println("⏪ Backtracking...")
      solveRec(problem, serializer, remainingRounds - 1, updatedMemory)
    }
  }



