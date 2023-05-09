package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Recipe(val name: String, val ingredients: List<String>)

suspend fun main() =
    ai {
        val recipe: Recipe = prompt("Recipe for chocolate chip cookies.")
        println("The recipe for ${recipe.name} is ${recipe.ingredients}")
    }.getOrElse { println(it) }
