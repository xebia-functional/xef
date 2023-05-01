package com.xebia.functiona.langchain4k.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Population(val size: Int, val description: String)

suspend fun main() {
    val cadiz: Population = ai("Population of Cádiz, Spain.")
    val seattle: Population = ai("Population of Seattle, WA.")
    println("The population of Cádiz is ${cadiz.size} and the population of Seattle is ${seattle.size}")
}
