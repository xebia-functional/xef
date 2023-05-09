package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.prompt
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Planet(val name: String, val distanceFromSun: Double, val moons: List<Moon>)

@Serializable
data class Moon(val name: String, val distanceFromPlanet: Double)

suspend fun main() =
    prompt {
      val earth: Planet = prompt("Information about Earth and its moon.")
      val mars: Planet = prompt("Information about Mars and its moons.")

      fun planetInfo(planet: Planet): String {
        return """${planet.name} is ${planet.distanceFromSun} million km away from the Sun.
              |It has the following moons:
              |${planet.moons.joinToString("\n") { "  - ${it.name}: ${it.distanceFromPlanet} km away from ${planet.name}" }}
              """.trimMargin()
      }

      println("Celestial bodies information:\n\n${planetInfo(earth)}\n\n${planetInfo(mars)}")
    }.getOrElse { println(it) }
