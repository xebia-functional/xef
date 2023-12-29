package com.xebia.functional.xef.dsl.images

import com.xebia.functional.xef.AI

suspend fun main() {
  val ai = AI.images()
  val marsImages = ai.image(prompt = "Event horizon in a black hole", amount = 1)
  marsImages.collect { image ->
    println("Image: $image")
    val marsVariants = ai.variant(image)
    marsVariants.collect { variant -> println("Variant: $variant") }
  }
}
