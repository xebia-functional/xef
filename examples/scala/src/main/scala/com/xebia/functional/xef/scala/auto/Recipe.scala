package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

private final case class Recipe(name: String, ingredients: List[String]) derives ScalaSerialDescriptor, Decoder

@main def runRecipe: Unit =
  ai {
    val recipe: Recipe = prompt("Recipe for chocolate chip cookies.")
    println(s"The recipe for ${recipe.name} is ${recipe.ingredients}")
  }.getOrElse(ex => println(ex.getMessage))
