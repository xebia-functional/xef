package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import com.xebia.functional.xef.scala.agents.DefaultSearch
import io.circe.Decoder
import io.circe.parser.decode

private final case class NumberOfMedicalNeedlesInWorld(numberOfNeedles: Long) derives ScalaSerialDescriptor, Decoder

@main def runDivergentTasks: Unit =
  ai {
    contextScope(DefaultSearch.search("Estimate amount of medical needles in the world")) {
      val needlesInWorld = prompt[NumberOfMedicalNeedlesInWorld]("Provide the number of medical needles in the world as an integer number")
      println(s"Needles in world: ${needlesInWorld.numberOfNeedles}")
    }
  }.getOrElse(ex => println(ex.getMessage))
