package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class MeaningOfLife(mainTheories: List[String]) derives SerialDescriptor, Decoder

@main def runMeaningOfLife: Unit =
  conversation {
    val meaningOfLife = prompt[MeaningOfLife](Prompt("What are the main theories about the meaning of life"))
    println(s"There are several theories about the meaning of life:\n ${meaningOfLife.mainTheories}")
  }
