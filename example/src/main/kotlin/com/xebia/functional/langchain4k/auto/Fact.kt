package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Fact(val topic: String, val content: String)

suspend fun main() {
    val fact1: Fact = ai("A fascinating fact about you")
    val fact2: Fact = ai("An interesting fact about me")

    val riddlePrompt = """
        Create a riddle that combines the following facts:

        Fact 1: ${fact1.content}
        Fact 2: ${fact2.content}
    """.trimIndent()

    val riddle: String = ai(riddlePrompt)

    println("Riddle:\n\n${riddle}")
}

