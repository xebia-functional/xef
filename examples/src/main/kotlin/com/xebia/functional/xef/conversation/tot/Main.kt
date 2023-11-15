package com.xebia.functional.xef.conversation.tot

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import kotlinx.serialization.Serializable

@Serializable data class FinalSolution(val solution: String)

suspend fun main() =
  OpenAI.conversation {
    val problem =
      Problem(
        """|
    |You are an expert functional programmer.
    |1. You never throw exceptions.
    |2. You never use null.
    |3. You never use `for` `while` or loops in general, prefer tail recursion.
    |4. You never use mutable state.
    |
    |This code is unsafe. Find the problems in this code and provide a Github suggestion code fence with the `diff` to fix it.
    |
    |```kotlin
    |fun access(list: List<Int>, index: Int): Int {
    |  return list[index]
    |}
    |```
    |
    |Return a concise solution that fixes the problems in the code.
  """
          .trimMargin()
      )
    val maxRounds = 5

    val solution = solve<FinalSolution>(problem, maxRounds)

    println("✅ Final solution: ${solution.answer}")
    println("✅ Solution validity: ${solution.isValid}")
    println("✅ Solution reasoning: ${solution.reasoning}")
    println("✅ Solution code: ${solution.value?.solution}")
  }
