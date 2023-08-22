package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class Colors(colors: List[String]) derives SerialDescriptor, Decoder

@main def runColors: Unit =
  conversation {
    val colors = prompt[Colors](Prompt("A selection of 10 beautiful colors that go well together"))
    println(colors)
  }
