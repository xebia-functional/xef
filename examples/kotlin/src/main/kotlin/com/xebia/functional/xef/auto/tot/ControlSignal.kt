package com.xebia.functional.xef.auto.tot

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class ControlSignal(val value: String)

// Function to generate the control signal based on the memory of previous results
internal suspend fun <A> Conversation.controlSignal(memory: Memory<A>): ControlSignal {
  println(
    "🧠 Generating control signal for problem: ${truncateText(memory.problem.description)}..."
  )
  val guidancePrompt =
    """|
    |You are an expert advisor on information extraction.
    |You generate guidance for a problem.
    |${renderHistory(memory)}
    |You are given the following problem:
    |${memory.problem.description}
    |Instructions:
    |1. Generate 1 guidance to get the best results for this problem.
    |2. Ensure the guidance is relevant to the problem.
    |3. Ensure the guidance is accurate, complete, and unambiguous.
    |4. Ensure the guidance is actionable.
    |5. Ensure the guidance accounts for previous answers in the `history`.
    |
  """
      .trimMargin()
  return prompt<String, ControlSignal>(guidancePrompt).also {
    println("🧠 Generated control signal: ${truncateText(it.value)}")
  }
}
