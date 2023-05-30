package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.agents.DefaultSearch
import io.circe.Decoder
import io.circe.parser.decode

private final case class NumberOfMedicalNeedlesInWorld(numberOfNeedles: String) derives ScalaSerialDescriptor, Decoder

@main def runDivergentTasks: Unit =
  val needlesInWorld: NumberOfMedicalNeedlesInWorld = ai {
    contextScope(DefaultSearch.search("Estimate amount of medical needles in the world")) {
      prompt[NumberOfMedicalNeedlesInWorld]("Provide the number of medical needles in the world")
    }
  }
  println(s"Needles in world: ${needlesInWorld.numberOfNeedles}")
