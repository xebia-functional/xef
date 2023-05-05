package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(val name: String, val recipes: List<Recipe>) {
  fun prettyPrint(): String {
    return recipes.joinToString("\n") { "${it.name}:\n${it.ingredients.joinToString("\n")}" }
  }
}

suspend fun main() {
  ai {
    val mealPlan: MealPlan =
      ai(
        "Meal plan for the week for a person with gall bladder stones that includes 5 recipes."
      )
    println(
      """The meal plan for the week is: 
        |${mealPlan.prettyPrint()}""".trimMargin()
    )
  }.getOrElse { println(it) }
}
