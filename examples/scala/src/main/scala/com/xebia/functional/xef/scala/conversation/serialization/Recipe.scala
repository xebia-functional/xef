package com.xebia.functional.xef.scala.conversation.serialization

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder

private final case class Recipe(name: String, ingredients: List[String]) derives SerialDescriptor, Decoder

@main def runRecipe: Unit =
  conversation {
    val recipe: Recipe = prompt(Prompt("Recipe for chocolate chip cookies."))
    println(s"The recipe for ${recipe.name} is ${recipe.ingredients}")
  }
