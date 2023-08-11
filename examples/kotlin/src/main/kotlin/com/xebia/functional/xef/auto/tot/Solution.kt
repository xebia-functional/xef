package com.xebia.functional.xef.auto.tot

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Solution<out A>(
  val answer: String,
  val isValid: Boolean,
  val reasoning: String,
  val value: A? = null
)

// Function to prompt the AI for a solution
internal suspend fun <A> Conversation.solution(
  serializer: KSerializer<Solution<A>>,
  memory: Memory<A>,
  controlSignal: ControlSignal
): Solution<A> {
  println("🔍 Generating solution for problem: ${truncateText(memory.problem.description)}...")
  // ai emoji
  println("🤖 Generating solution for problem: ${truncateText(memory.problem.description)}...")
  val enhancedPrompt =
    """|
       |Given previous history:
       |${renderHistory(memory)}
       |Given the goal: 
       |```goal
       |${memory.problem.description} 
       |```
       |and considering the guidance: 
       |```guidance
       |${controlSignal.value}
       |```
       |
       |Instructions:
       |
       |1. Please provide a comprehensive solution. 
       |2. Consider all possible scenarios and edge cases. 
       |3. Ensure your solution is accurate, complete, and unambiguous. 
       |4. If you are unable to provide a solution, please provide a reason why and set `isValid` to false.
       |5. Include citations, references and links at the end to support your solution based on your sources.
       |6. Do not provide recommendations, only provide a solution.
       |7. when `isValid` is true Include in the `value` field the value of the solution according to the `value` json schema.
       |8. If no solution is found set the `value` field to `null`.
       |9. If the solution is not valid set the `isValid` field to `false` and the `value` field to `null`.
       |10. If the solution is valid set the `isValid` field to `true` and the `value` field to the value of the solution.
       |
       |"""
      .trimMargin()
  return prompt(OpenAI().DEFAULT_SERIALIZATION, Prompt(enhancedPrompt), serializer).also {
    println("🤖 Generated solution: ${truncateText(it.answer)}")
  }
}
