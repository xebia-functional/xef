package com.xebia.functional.xef.dsl.vision

import com.xebia.functional.xef.AI

suspend fun main() {
  val images = AI.images()
  val stream =
    images.vision(
      prompt = "Describe the image in detail",
      url = "https://apod.nasa.gov/apod/image/2401/ngc1232b_vlt_960.jpg"
    )
  stream.collect(::print)
}
