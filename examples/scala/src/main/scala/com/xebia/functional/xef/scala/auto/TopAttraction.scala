package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class City(name: String, country: String) derives SerialDescriptor, Decoder

private final case class TopAttractionWeather(city: City, temperature: Double, description: String) derives SerialDescriptor, Decoder

private final case class TopAttraction(city: City, attractionName: String, description: String, weather: TopAttractionWeather)
    derives SerialDescriptor,
      Decoder

@main def runTopAttraction: Unit =
  conversation {
    val nearbyTopAttraction: TopAttraction = prompt(Prompt("Top attraction in Cádiz, Spain."))
    println(
      s"""
        |The top attraction in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.attractionName}.
        |Here's a brief description: ${nearbyTopAttraction.description}.
        |The weather in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.weather.temperature} degrees Celsius and ${nearbyTopAttraction.weather.description}.
        |""".stripMargin
    )
  }
