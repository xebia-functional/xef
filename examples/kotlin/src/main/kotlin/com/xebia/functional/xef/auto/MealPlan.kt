package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.search
import com.xebia.functional.xef.auto.llm.openai.conversation
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(val name: String, val recipes: List<Recipe>)

suspend fun main() {
  conversation {
    addContext(search("gall bladder stones meals"))
    val mealPlan: MealPlan = prompt(
      "Meal plan for the week for a person with gall bladder stones that includes 5 recipes."
    )
    println(mealPlan)
  }
}
