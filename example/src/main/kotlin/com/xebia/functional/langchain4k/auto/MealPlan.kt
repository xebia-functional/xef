package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import com.xebia.functional.tool.search
import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(val name: String, val recipes: List<Recipe>)

suspend fun main() {
    ai {
        agent(search("gall bladder stones meals")) {
            val mealPlan: MealPlan = ai(
                "Meal plan for the week for a person with gall bladder stones that includes 5 recipes."
            )
            println(mealPlan)
        }
    }.getOrElse { println(it) }
}
