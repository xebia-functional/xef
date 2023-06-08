package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class TouristAttraction(name: String, location: String, history: String) derives SerialDescriptor, Decoder

@main def runTouristAttraction: Unit =
  ai {
    val statueOfLiberty: TouristAttraction = prompt("Statue of Liberty location and history.")
    println(
      s"""
         |${statueOfLiberty.name} is located in ${statueOfLiberty.location} and has the following history:
         |${statueOfLiberty.history}
      """.stripMargin
    )
  }.getOrElse(ex => println(ex.getMessage))
