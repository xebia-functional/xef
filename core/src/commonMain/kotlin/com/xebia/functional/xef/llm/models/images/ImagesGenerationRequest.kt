package com.xebia.functional.xef.llm.models.images

import com.xebia.functional.xef.prompt.Prompt

data class ImagesGenerationRequest(
  val prompt: Prompt,
  val numberImages: Int = 1,
  val size: String = "1024x1024",
  val responseFormat: String = "url",
  val user: String? = null
)
