package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode

private final case class City(name: String, country: String) derives ScalaSerialDescriptor, Decoder

private final case class TopAttractionWeather(city: City, temperature: Double, description: String) derives ScalaSerialDescriptor, Decoder

private final case class TopAttraction(city: City, attractionName: String, description: String, weather: TopAttractionWeather)
    derives ScalaSerialDescriptor,
      Decoder

@main def runTopAttraction: Unit =
  ai {
    val nearbyTopAttraction: TopAttraction = prompt("Top attraction in Cádiz, Spain.")
    println(
      s"""
        |The top attraction in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.attractionName}.
        |Here's a brief description: ${nearbyTopAttraction.description}.
        |The weather in ${nearbyTopAttraction.city.name} is ${nearbyTopAttraction.weather.temperature} degrees Celsius and ${nearbyTopAttraction.weather.description}.
        |""".stripMargin
    )
  }.getOrElse(ex => println(ex.getMessage))
