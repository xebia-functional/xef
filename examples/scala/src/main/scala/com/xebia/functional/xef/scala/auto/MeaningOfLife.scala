package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class MeaningOfLife(mainTheories: List[String]) derives SerialDescriptor, Decoder

@main def runMeaningOfLife: Unit =
  conversation {
    val meaningOfLife = prompt[MeaningOfLife]("What are the main theories about the meaning of life")
    println(s"There are several theories about the meaning of life:\n ${meaningOfLife.mainTheories}")
  }
