package com.xebia.functional.examples.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Recipe(val name: String, val ingredients: List<String>)

suspend fun main() {
    val recipe: Recipe = ai("Recipe for chocolate chip cookies.")
    println("The recipe for ${recipe.name} is ${recipe.ingredients}")
}
