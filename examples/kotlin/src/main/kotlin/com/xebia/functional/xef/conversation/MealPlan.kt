package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import kotlinx.serialization.Serializable

@Serializable data class MealPlan(val name: String, val recipes: List<Recipe>)

suspend fun main() {
  OpenAI.conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("gall bladder stones meals"))
    val mealPlan: MealPlan =
      prompt(
        "Meal plan for the week for a person with gall bladder stones that includes 5 recipes."
      )
    println(mealPlan)
  }
}
