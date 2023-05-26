package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Population(size: Int, description: String) derives ScalaSerialDescriptor, Decoder

private final case class Image(description: String, url: String) derives ScalaSerialDescriptor, Decoder

@main def runPopulation: Unit =
  ai {
    val cadiz: Population = prompt("Population of Cádiz, Spain.")
    val seattle: Population = prompt("Population of Seattle, WA.")
    println(s"The population of Cádiz is ${cadiz.size} and the population of Seattle is ${seattle.size}")
  }
