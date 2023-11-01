package com.xebia.functional.xef.examples.scala.iteration

import com.xebia.functional.xef.prompt.JvmPromptBuilder
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

case class Animal(name: String, habitat: String, diet: String) derives SerialDescriptor, Decoder
case class Invention(name: String, inventor: String, year: Int, purpose: String) derives SerialDescriptor, Decoder

@main def runAnimalStory(): Unit = conversation:
  val animal = prompt[Animal]("A unique animal species")
  val invention = prompt[Invention]("A groundbreaking invention from the 20th century.")
  println(s"Animal: $animal")
  println(s"Invention: $invention")
  val builder = new JvmPromptBuilder()
    .addSystemMessage("You are a writer for a science fiction magazine.")
    .addUserMessage("Write a short story of 200 words that involves the animal and the invention.")
  val story = promptMessage(builder.build)
  println(story)
