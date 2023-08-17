package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class Recipe(name: String, ingredients: List[String]) derives SerialDescriptor, Decoder

@main def runRecipe: Unit =
  conversation {
    val recipe: Recipe = prompt(Prompt("Recipe for chocolate chip cookies."))
    println(s"The recipe for ${recipe.name} is ${recipe.ingredients}")
  }
