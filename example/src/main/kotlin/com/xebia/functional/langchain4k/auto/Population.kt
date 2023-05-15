package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Population(val size: Int, val description: String)

@Serializable
data class Image(
    val description: String,
    val url: String,
)

suspend fun main() =
    ai {
        val cadiz: Population = prompt("Population of Cádiz, Spain.")
        val seattle: Population = prompt("Population of Seattle, WA.")
        val img: Image = image("A hybrid city of Cádiz, Spain and Seattle, US.")
        println(img)
        println("The population of Cádiz is ${cadiz.size} and the population of Seattle is ${seattle.size}")
    }.getOrElse { println(it) }
