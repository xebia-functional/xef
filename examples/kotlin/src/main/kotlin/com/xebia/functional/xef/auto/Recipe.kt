package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Recipe(val name: String, val ingredients: List<String>)

suspend fun main() =
  OpenAI.conversation {
    val recipe: Recipe = prompt("Recipe for chocolate chip cookies.")
    println("The recipe for ${recipe.name} is ${recipe.ingredients}")
  }
