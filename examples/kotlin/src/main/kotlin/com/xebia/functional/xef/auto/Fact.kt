package com.xebia.functional.xef.auto

import kotlinx.serialization.Serializable

@Serializable
data class Fact(val topic: String, val content: String)

suspend fun main() {
    ai {
      val fact1: Fact = prompt("A fascinating fact about you")
      val fact2: Fact = prompt("An interesting fact about me")

      val riddlePrompt = """
          Create a riddle that combines the following facts:
  
          Fact 1: ${fact1.content}
          Fact 2: ${fact2.content}
      """.trimIndent()

      val riddle: String = prompt(riddlePrompt)

      println("Riddle:\n\n${riddle}")
    }.getOrElse { println(it) }
}

