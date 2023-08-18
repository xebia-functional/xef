package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class TopAttraction(
  val city: City,
  val attractionName: String,
  val description: String,
  val weather: Weather
)

@Serializable data class City(val name: String, val country: String)

@Serializable data class Weather(val city: City, val temperature: Double, val description: String)

suspend fun main() =
  OpenAI.conversation {
    val nearbyTopAttraction: TopAttraction = prompt("Top attraction in Cádiz, Spain.")
    println(
      """
          |The top attraction in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.attractionName}. 
          |Here's a brief description: ${nearbyTopAttraction.description}.
          |The weather in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.weather.temperature} degrees Celsius and ${nearbyTopAttraction.weather.description}.
          |"""
        .trimMargin()
    )
  }
