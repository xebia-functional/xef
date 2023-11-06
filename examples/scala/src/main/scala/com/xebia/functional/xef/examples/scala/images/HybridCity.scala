package com.xebia.functional.xef.examples.scala.images

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

@main def runHybridCity(): Unit = conversation:
  val imageUrls = images(Prompt("A hybrid city of Cádiz, Spain and Seattle, US."))
  println(imageUrls)
