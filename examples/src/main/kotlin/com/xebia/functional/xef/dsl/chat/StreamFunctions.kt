package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.llm.StreamedFunction
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class SolarSystemPlanet(
  val name: String,
  val distanceFromSun: Double,
  val mass: Double,
  val radius: Double,
  val gravity: Double,
  val lengthOfDay: Double,
  val orbitalPeriod: Double,
  val meanTemperature: Double,
  val numberOfMoons: Int,
  val hasRingSystem: Boolean
)

suspend fun main() {
  val result: Flow<StreamedFunction<SolarSystemPlanet>> = AI("List of planets in the solar system")
  result.collect { println(it) }
}
