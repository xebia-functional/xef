package com.xebia.functional.xef.dsl.vision

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.vision

suspend fun main() {
  val openAI = OpenAI(logRequests = false)
  val stream =
    openAI.chat.vision(
      prompt = "Describe the image in detail",
      url = "https://apod.nasa.gov/apod/image/2401/ngc1232b_vlt_960.jpg"
    )
  stream.collect(::print)
}
