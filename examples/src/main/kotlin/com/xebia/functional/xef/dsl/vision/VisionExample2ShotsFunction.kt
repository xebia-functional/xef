package com.xebia.functional.xef.dsl.vision

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.visionStructured
import kotlinx.serialization.Serializable

@Serializable
data class ImageAnalysisResult(
  val topic: String,
  val description: String,
)

suspend fun main() {
  val openAI = OpenAI()
  val result: ImageAnalysisResult =
    openAI.chat.visionStructured(
      prompt = "Describe the image in detail",
      url = "https://apod.nasa.gov/apod/image/2401/ngc1232b_vlt_960.jpg"
    )
  println(result)
}
