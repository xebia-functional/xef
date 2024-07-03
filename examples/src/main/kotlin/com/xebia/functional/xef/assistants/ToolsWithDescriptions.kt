package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.assistants.Tool
import kotlinx.serialization.Serializable

@Description("Natural numbers")
enum class NaturalWithDescriptions {
  @Description("If the number is positive.") POSITIVE,
  @Description("If the number is negative.") NEGATIVE
}

@Serializable
data class SumInputWithDescription(
  @Description("Left operand") val left: Int,
  @Description("Right operand") val right: Int,
  val natural: NaturalWithDescriptions
)

class SumToolWithDescription : Tool<SumInputWithDescription, Int> {
  override suspend fun invoke(input: SumInputWithDescription): Int {
    return input.left + input.right
  }
}

suspend fun main() {
  val toolConfig = Tool.toolOf(SumToolWithDescription()).functionObject
  println(toolConfig.parameters)
}
