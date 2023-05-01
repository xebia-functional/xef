package com.xebia.functiona.langchain4k.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class MeaningOfLife(val mainTheories: List<String>)

suspend fun main() {
    val meaningOfLife: MeaningOfLife = ai("What are the main theories about the meaning of life")
    println("There are several theories about the meaning of life:\n ${meaningOfLife.mainTheories}")
}
