package com.xebia.functional.xef.scala.conversation.conversations

import com.xebia.functional.xef.prompt.{JvmPromptBuilder, Prompt}
import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder

private final case class Animal(name: String, habitat: String, diet: String) derives SerialDescriptor, Decoder

private final case class Invention(name: String, inventor: String, year: Int, purpose: String) derives SerialDescriptor, Decoder

@main def runAnimal: Unit =
  conversation {
    val animal: Animal = prompt(Prompt("A unique animal species"))
    val invention: Invention = prompt(Prompt("A groundbreaking invention from the 20th century."))

    println(s"Animal: $animal")
    println(s"Invention: $invention")

    val builder = new JvmPromptBuilder()
      .addSystemMessage("You are a writer for a science fiction magazine.")
      .addUserMessage("Write a short story of 200 words that involves the animal and the invention")

    val story = promptMessage(builder.build())

    println(story)
  }
