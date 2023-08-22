package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class TouristAttraction(name: String, location: String, history: String) derives SerialDescriptor, Decoder

@main def runTouristAttraction: Unit = conversation {
  val statueOfLiberty: TouristAttraction = prompt(Prompt("Statue of Liberty location and history."))
  println(
    s"""
       |${statueOfLiberty.name} is located in ${statueOfLiberty.location} and has the following history:
       |${statueOfLiberty.history}
      """.stripMargin
  )
}
