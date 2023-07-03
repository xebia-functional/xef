package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class TopAttraction(val city: City, val attractionName: String, val description: String, val weather: Weather)

@Serializable
data class City(val name: String, val country: String)

@Serializable
data class Weather(val city: City, val temperature: Double, val description: String)

suspend fun main() = ai {
  val nearbyTopAttraction: TopAttraction = prompt("Top attraction in Cádiz, Spain.")
  println(
      """
          |The top attraction in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.attractionName}. 
          |Here's a brief description: ${nearbyTopAttraction.description}.
          |The weather in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.weather.temperature} degrees Celsius and ${nearbyTopAttraction.weather.description}.
          |""".trimMargin()
  )
}.getOrElse { println(it) }
