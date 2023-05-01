package com.xebia.functional.examples.auto

import com.xebia.functional.auto.agents.WikipediaAgent
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(val name: String, val recipes: List<Recipe>) {
    fun prettyPrint(): String {
        return recipes.joinToString("\n") { "${it.name}:\n${it.ingredients.joinToString("\n")}" }
    }
}

suspend fun main() {
    val mealPlan: MealPlan =
        ai("Meal plan for the week for a person with gall bladder stones that includes 5 recipes.",
            auto = true,
            agents = listOf(WikipediaAgent))
    println(
        """The meal plan for the week is: 
        |${mealPlan.prettyPrint()}""".trimMargin()
    )
}
