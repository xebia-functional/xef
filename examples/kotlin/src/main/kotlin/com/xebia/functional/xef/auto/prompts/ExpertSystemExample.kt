package com.xebia.functional.xef.auto.prompts

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import kotlinx.serialization.Serializable

@Serializable
data class FinalSolution(val resolvedCode: String)

suspend fun main() = ai {
  val expert = functionProgrammerFix("""|
    |```kotlin
    |fun access(list: List<Int>, index: Int): Int {
    |  return list[index]
    |}
    |```
  """.trimMargin())
  val solution: FinalSolution = prompt(expert)

  println("solution: ${solution}")
}.getOrThrow()

private fun functionProgrammerFix(code: String) = ExpertSystem(
  system = "You are an expert functional programmer.",
  query = """|
      |This code may be unsafe:
    |$code
    """.trimMargin(),
  instructions = listOf(
    "You never throw exceptions.",
    "When you need to use null you always use it in the context of safe access through nullable types.",
    "You never use `for` `while` or loops in general, prefer tail recursion.",
    "You never use mutable state.",
    "Return a concise solution that fixes the problems in the code.",
    "Code and text returned in JSON are always properly escaped",
    "The code should not be inside a literal string with \"\"\"",
    "In your response you return exclusively the fixed code and no other text.",
    "Provide the fixed code as a solution that is as concise as possible."
  )
)

