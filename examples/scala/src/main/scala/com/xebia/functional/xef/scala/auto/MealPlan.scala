package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.agents.DefaultSearch
import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

private final case class MealPlanRecipe(name: String, ingredients: List[String]) derives ScalaSerialDescriptor, Decoder

private final case class MealPlan(name: String, recipes: List[MealPlanRecipe]) derives ScalaSerialDescriptor, Decoder

@main def runMealPlan: Unit =
  ai {
    contextScope(DefaultSearch.search("gall bladder stones meals")) {
      val mealPlan = prompt[MealPlan]("Meal plan for the week for a person with gall bladder stones that includes 5 recipes.")
      println(mealPlan)
    }
  }.getOrElse(ex => println(ex.getMessage))
