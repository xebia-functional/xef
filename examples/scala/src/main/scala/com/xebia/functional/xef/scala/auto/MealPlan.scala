package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import io.circe.Decoder

private final case class MealPlanRecipe(name: String, ingredients: List[String]) derives SerialDescriptor, Decoder

private final case class MealPlan(name: String, recipes: List[MealPlanRecipe]) derives SerialDescriptor, Decoder

@main def runMealPlan: Unit =
  conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, summon[ScalaConversation], 3)
    addContext(search.search("gall bladder stones meals").get())
    val mealPlan = prompt[MealPlan]("Meal plan for the week for a person with gall bladder stones that includes 5 recipes.")
    println(mealPlan)
  }
