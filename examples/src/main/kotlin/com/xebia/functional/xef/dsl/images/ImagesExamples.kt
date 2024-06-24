package com.xebia.functional.xef.dsl.images

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.asInputProvider
import io.github.nomisrev.openapi.CreateImageRequest

suspend fun main() {
  val openAI = OpenAI(logRequests = true)
  val ai = openAI.images
  val image =
    ai.createImage(
      createImageRequest = CreateImageRequest(prompt = "Event horizon in a black hole")
    )
  val generatedImage = image.data.first()
  println("Image: $generatedImage")
  val variant = ai.createImageVariation(generatedImage.asInputProvider())
  println("Variant: $variant")
}
