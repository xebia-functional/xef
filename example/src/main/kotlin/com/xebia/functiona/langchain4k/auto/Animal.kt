package com.xebia.functiona.langchain4k.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Animal(val name: String, val habitat: String, val diet: String)

@Serializable
data class Invention(val name: String, val inventor: String, val year: Int, val purpose: String)

suspend fun main() {
    val animal: Animal = ai("A unique animal species.")
    val invention: Invention = ai("A groundbreaking invention from the 20th century.")

    val storyPrompt = """
        Write a short story that involves the following elements:

        1. A unique animal species called ${animal.name} that lives in ${animal.habitat} and has a diet of ${animal.diet}.
        2. A groundbreaking invention from the 20th century called ${invention.name}, invented by ${invention.inventor} in ${invention.year}, which serves the purpose of ${invention.purpose}.
    """.trimIndent()

    val story: String = ai(storyPrompt)

    println("Short Story:\n\n${story}")
}
