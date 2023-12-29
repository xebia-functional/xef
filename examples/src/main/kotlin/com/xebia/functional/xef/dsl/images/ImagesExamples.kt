package com.xebia.functional.xef.dsl.images

import com.xebia.functional.xef.AI

suspend fun main() {
  val ai = AI.images()
  val images = ai.image(prompt = "Event horizon in a black hole", amount = 1)
  images.collect { image ->
    println("Image: $image")
    val variants = ai.variant(image)
    variants.collect { variant -> println("Variant: $variant") }
  }
}
