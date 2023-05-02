package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.agents.Agent
import com.xebia.functional.auto.agents.wikipedia
import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(val name: String, val recipes: List<Recipe>) {
  fun prettyPrint(): String {
    return recipes.joinToString("\n") { "${it.name}:\n${it.ingredients.joinToString("\n")}" }
  }
}

suspend fun main() {
  val mealPlan: MealPlan =
    ai(
      "Meal plan for the week for a person with gall bladder stones that includes 5 recipes.",
      auto = true,
      agents = listOf(Agent.wikipedia())
    )
  println(
    """The meal plan for the week is: 
        |${mealPlan.prettyPrint()}""".trimMargin()
  )
}
