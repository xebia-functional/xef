package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.prompt
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Population(val size: Int, val description: String)

suspend fun main() =
    prompt {
        val cadiz: Population = prompt("Population of Cádiz, Spain.")
        val seattle: Population = prompt("Population of Seattle, WA.")
        println("The population of Cádiz is ${cadiz.size} and the population of Seattle is ${seattle.size}")
    }.getOrElse { println(it) }
