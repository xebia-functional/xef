package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import io.circe.Decoder

private final case class Animal(name: String, habitat: String, diet: String) derives SerialDescriptor, Decoder

private final case class Invention(name: String, inventor: String, year: Int, purpose: String) derives SerialDescriptor, Decoder

private final case class Story(animal: Animal, invention: Invention, story: String) derives SerialDescriptor, Decoder

@main def runAnimal: Unit =
  conversation {
    val animal: Animal = prompt(Prompt("A unique animal species"))
    val invention: Invention = prompt(Prompt("A groundbreaking invention from the 20th century."))

    val builder = new PromptBuilder()
      .addUserMessage("Write a short story of 500 words that involves the following elements:")
      .addUserMessage(s"1. A unique animal species called ${animal.name} that lives in ${animal.habitat} and has a diet of ${animal.diet}.")
      .addUserMessage(
        s"2. A groundbreaking invention from the 20th century called ${invention.name} , invented by ${invention.inventor} in ${invention.year}, which serves the purpose of ${invention.purpose}."
      )

    val story: Story = prompt(builder.build())

    println(s"Story about ${animal.name} and ${invention.name}: ${story.story}")
  }
