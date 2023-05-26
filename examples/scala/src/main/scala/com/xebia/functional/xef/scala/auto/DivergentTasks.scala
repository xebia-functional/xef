package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode

private final case class NumberOfMedicalNeedlesInWorld(numberOfNeedles: String) derives ScalaSerialDescriptor, Decoder

@main def runDivergentTasks: Unit =
  val needlesInWorld = ai(prompt[NumberOfMedicalNeedlesInWorld]("Provide the number of medical needles in the world"))
  println(s"Needles in world: ${needlesInWorld.numberOfNeedles}")
