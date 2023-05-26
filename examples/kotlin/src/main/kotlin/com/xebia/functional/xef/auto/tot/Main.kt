package com.xebia.functional.xef.auto.tot

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrThrow
import kotlinx.serialization.Serializable

@Serializable
data class KotlinSourceCode(val sources: String)

suspend fun main() = ai {
  val problem = Problem("""|
    |You are an expert functional programmer.
    |1. You never throw exceptions.
    |2. You never use null.
    |3. You never use `for` `while` or loops in general, prefer tail recursion.
    |4. You never use mutable state.
    |
    |Find the problems in this code and provide a Github suggestion code fence with the `diff` to fix it.
    |
    |```kotlin
    |fun access(list: List<Int>, index: Int): Int {
    |  return list[index]
    |}
    |```
    |
    |Return a concise solution that fixes the problems in the code.
  """.trimMargin())
  val maxRounds = 5

  val solution = solve<KotlinSourceCode>(problem, maxRounds)

  println("✅ Final solution: ${solution.answer}")
  println("✅ Solution validity: ${solution.isValid}")
  println("✅ Solution reasoning: ${solution.reasoning}")
  println("✅ Solution code: ${solution.value?.sources}")
}.getOrThrow()

