package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import io.circe.Decoder

private final case class Population(size: Int, description: String) derives SerialDescriptor, Decoder

private final case class Image(description: String, url: String) derives SerialDescriptor, Decoder

@main def runPopulation: Unit =
  conversation {
    val cadiz: Population = prompt("Population of Cádiz, Spain.")
    val seattle: Population = prompt("Population of Seattle, WA.")
    val imgs: ImagesGenerationResponse = images("A hybrid city of Cádiz, Spain and Seattle, US.")
    println(imgs)
    println(s"The population of Cádiz is ${cadiz.size} and the population of Seattle is ${seattle.size}")
  }
