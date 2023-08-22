package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class Poem(title: String, content: String) derives SerialDescriptor, Decoder

@main def runPoem: Unit =
  conversation {
    val poem1: Poem = prompt(Prompt("A short poem about the beauty of nature."))
    val poem2: Poem = prompt(Prompt("A short poem about the power of technology."))
    val poem3: Poem = prompt(Prompt("A short poem about the wisdom of artificial intelligence."))

    val combinedPoemContent = s"${poem1.content}\n\n${poem2.content}\n\n${poem3.content}"

    val newPoemPrompt = Prompt("""
          Write a new poem that combines ideas from the following themes: the beauty of nature, the power of technology, and the wisdom of artificial intelligence. Here are some examples of poems on these themes:

          $combinedPoemContent
      """.stripMargin)

    val newPoem: Poem = prompt(newPoemPrompt)

    println(s"New Poem:\n\n${newPoem.content}")
  }
