package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Fact(topic: String, content: String) derives SerialDescriptor, Decoder

private final case class Riddle(content: String) derives SerialDescriptor, Decoder

@main def runFact: Unit =
  conversation {
    val fact1 = prompt[Fact]("A fascinating fact about you")
    val fact2 = prompt[Fact]("An interesting fact about me")

    val riddlePrompt =
      s"""
        |Create a riddle that combines the following facts:
        |Fact 1: ${fact1.content}
        |Fact 2: ${fact2.content}
      """.stripMargin

    val riddle = prompt[Riddle](riddlePrompt)

    println(s"Riddle:\n${riddle.content}")
  }
