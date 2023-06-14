package com.xebia.functional.xef.llm.openai.images

import kotlinx.serialization.Serializable

@Serializable
data class ImagesGenerationResponse(val created: Long, val data: List<ImageGenerationUrl>)
