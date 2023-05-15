package com.xebia.functional.xef.auto

import kotlinx.serialization.Serializable

@Serializable
data class MeaningOfLife(val mainTheories: List<String>)

suspend fun main() {
    ai {
        val meaningOfLife: MeaningOfLife = prompt("What are the main theories about the meaning of life")
        println("There are several theories about the meaning of life:\n ${meaningOfLife.mainTheories}")
    }.getOrElse { println(it) }
}
