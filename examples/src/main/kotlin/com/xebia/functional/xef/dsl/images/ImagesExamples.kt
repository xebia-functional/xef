package com.xebia.functional.xef.dsl.images

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.asInputProvider
import com.xebia.functional.xef.openapi.CreateImageRequest
import com.xebia.functional.xef.openapi.CreateImageVariationRequest

suspend fun main() {
  val openAI = OpenAI(logRequests = true)
  val ai = openAI.images
  val image = ai.generations.createImage(CreateImageRequest("Event horizon in a black hole"))
  val generatedImage = image.data.first()
  println("Image: $generatedImage")
  val variant =
    ai.variations.createImageVariation(
      CreateImageVariationRequest(generatedImage.asInputProvider())
    )
  println("Variant: $variant")
}
