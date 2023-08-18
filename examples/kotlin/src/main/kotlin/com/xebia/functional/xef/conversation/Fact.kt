package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Fact(val topic: String, val content: String)

@Serializable data class Riddle(val fact1: Fact, val fact2: Fact, val riddle: String)

suspend fun main() {
  OpenAI.conversation {
    val fact1: Fact = prompt("A fascinating fact about you")
    val fact2: Fact = prompt("An interesting fact about me")

    val riddlePrompt =
      """
          Create a riddle that combines the following facts:
  
          Fact 1: ${fact1.content}
          Fact 2: ${fact2.content}
      """
        .trimIndent()

    val riddle: Riddle = prompt(riddlePrompt)

    println("Riddle:\n\n${riddle}")
  }
}
