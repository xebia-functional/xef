package com.xebia.functional.xef.dsl.vision

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable
data class ImageAnalysisResult(
  val topic: String,
  val description: String,
)

suspend fun main() {
  val images = AI.images()
  val result: ImageAnalysisResult =
    images.visionStructured(
      prompt = "Describe the image in detail",
      url = "https://apod.nasa.gov/apod/image/2401/ngc1232b_vlt_960.jpg"
    )
  println(result)
}
