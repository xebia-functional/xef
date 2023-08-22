package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class Moon(name: String, distanceFromPlanet: Double) derives SerialDescriptor, Decoder

private final case class Planet(name: String, distanceFromSun: Double, moons: List[Moon]) derives SerialDescriptor, Decoder

@main def runPlanet: Unit =

  def planetInfo(planet: Planet): String = {
    s"""${planet.name} is ${planet.distanceFromSun} million km away from the Sun.
       |It has the following moons:
       |${planet.moons.map(it => s"${it.name}: ${it.distanceFromPlanet} km away from ${planet.name}").mkString("\n")}
    """.stripMargin
  }
  conversation {
    val earth: Planet = prompt(Prompt("Information about Earth and its moon."))
    val mars: Planet = prompt(Prompt("Information about Mars and its moons."))

    println(s"Celestial bodies information:\n\n${planetInfo(earth)}\n\n${planetInfo(mars)}")
  }
