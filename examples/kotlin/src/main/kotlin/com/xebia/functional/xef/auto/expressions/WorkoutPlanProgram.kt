package com.xebia.functional.xef.auto.expressions

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.expressions.Expression
import com.xebia.functional.xef.prompt.expressions.ExpressionResult

suspend fun workoutPlan(
  scope: CoreAIScope,
  model: ChatWithFunctions,
  goal: String,
  experienceLevel: String,
  equipment: String,
  timeAvailable: Int
): ExpressionResult = Expression.run(scope = scope, model = model, block = {
  system { "You are a personal fitness trainer" }
  user {
    """
     |I want to achieve $goal.
     |My experience level is $experienceLevel, and I have access to the following equipment: $equipment.
     |I can dedicate $timeAvailable minutes per day.
     |Can you create a workout plan for me?
  """.trimMargin()
  }
  assistant {
    """
     |Sure! Based on your goal, experience level, equipment available, and time commitment, here's a customized workout plan:
     |${prompt("workout_plan")}
  """.trimMargin()
  }
})

suspend fun main() {
  val model = OpenAI.DEFAULT_SERIALIZATION
  ai {
    val plan = workoutPlan(
      scope = this,
      model = model,
      goal = "building muscle",
      experienceLevel = "intermediate",
      equipment = "dumbbells, bench, resistance bands",
      timeAvailable = 45
    )
    println("--------------------")
    println("Workout Plan")
    println("--------------------")
    println("🤖 replaced: ${plan.values.replacements.joinToString { it.key }}")
    println("--------------------")
    println(plan.result)
    println("--------------------")
  }.getOrThrow()
}
