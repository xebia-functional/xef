package com.xebia.functional.xef.llm.models.images

data class ImagesGenerationRequest(
  val prompt: String,
  val numberImages: Int = 1,
  val size: String = "1024x1024",
  val responseFormat: String = "url",
  val user: String? = null
)
