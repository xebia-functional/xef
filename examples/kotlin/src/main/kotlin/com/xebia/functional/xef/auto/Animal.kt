package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.conversation
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Animal(val name: String, val habitat: String, val diet: String)

@Serializable
data class Invention(val name: String, val inventor: String, val year: Int, val purpose: String)

@Serializable
data class Story(val animal: Animal, val invention: Invention, val shortStory: String)

suspend fun main() {
  conversation {
    val animal: Animal = prompt("A unique animal species.")
    val invention: Invention = prompt("A groundbreaking invention from the 20th century.")

    val storyPrompt =
      """
          Write a short story of 500 words that involves the following elements:

          1. A unique animal species called ${animal.name} that lives in ${animal.habitat} and has a diet of ${animal.diet}.
          2. A groundbreaking invention from the 20th century called ${invention.name}, invented by ${invention.inventor} in ${invention.year}, which serves the purpose of ${invention.purpose}.
      """
        .trimIndent()

    val story: Story = prompt(storyPrompt)

    println("Story about ${animal.name} and ${invention.name}: ${story.shortStory}")
  }
}
