package com.xebia.functional.xef.scala.auto.images

import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Population(size: Int, description: String) derives SerialDescriptor, Decoder

private final case class Image(description: String, url: String) derives SerialDescriptor, Decoder

@main def runPopulation: Unit =
  conversation {
    val cadiz: Population = prompt(Prompt("Population of Cádiz, Spain."))
    val seattle: Population = prompt(Prompt("Population of Seattle, WA."))
    val imgs: ImagesGenerationResponse = images(Prompt("A hybrid city of Cádiz, Spain and Seattle, US."))
    println(imgs)
    println(s"The population of Cádiz is ${cadiz.size} and the population of Seattle is ${seattle.size}")
  }
