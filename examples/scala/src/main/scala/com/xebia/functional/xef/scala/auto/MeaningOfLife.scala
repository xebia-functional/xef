package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

private final case class MeaningOfLife(mainTheories: List[String]) derives ScalaSerialDescriptor, Decoder

@main def runMeaningOfLife: Unit =
  val meaningOfLife = ai(prompt[MeaningOfLife]("What are the main theories about the meaning of life"))
  println(s"There are several theories about the meaning of life:\n ${meaningOfLife.mainTheories}")
