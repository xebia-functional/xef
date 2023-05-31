package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode

private final case class Animal(name: String, habitat: String, diet: String) derives ScalaSerialDescriptor, Decoder

private final case class Invention(name: String, inventor: String, year: Int, purpose: String) derives ScalaSerialDescriptor, Decoder

private final case class Story(animal: Animal, invention: Invention, story: String) derives ScalaSerialDescriptor, Decoder

@main def runAnimal: Unit =
  ai {
    val animal: Animal = prompt("A unique animal species")
    val invention: Invention = prompt("A groundbreaking invention from the 20th century.")

    val storyPrompt =
      """
          Write a short story that involves the following elements:
          1. A unique animal species called ${animal.name} that lives in ${animal.habitat} and has a diet of ${animal.diet}.
          2. A groundbreaking invention from the 20th century called ${invention.name}, invented by ${invention.inventor} in ${invention.year}, which serves the purpose of ${invention.purpose}.
      """

    val story: Story = prompt(storyPrompt)

    println(s"Story about ${animal.name} and ${invention.name}: ${story.story}")
  }.getOrElse(ex => println(ex.getMessage))
