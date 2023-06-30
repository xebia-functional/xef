package com.xebia.functional.xef.auto

import kotlinx.serialization.Serializable

@Serializable
data class Planet(val name: String, val distanceFromSunInMillionKm: Double, val moons: List<Moon>)

@Serializable
data class Moon(val name: String, val distanceFromPlanetInKm: Double)

suspend fun main() =
    ai {
      val earth: Planet = prompt("Information about Earth and its moon.")
      val mars: Planet = prompt("Information about Mars and its moons.")

      fun planetInfo(planet: Planet): String {
        return """${planet.name} is ${planet.distanceFromSunInMillionKm} million km away from the Sun.
              |It has the following moons:
              |${planet.moons.joinToString("\n") { "  - ${it.name}: ${it.distanceFromPlanetInKm} km away from ${planet.name}" }}
              """.trimMargin()
      }

      println("Celestial bodies information:\n\n${planetInfo(earth)}\n\n${planetInfo(mars)}")
    }.getOrElse { println(it) }
