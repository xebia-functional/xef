package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.CreateImageRequest
import com.xebia.functional.openai.models.ImagesResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.VectorStore

interface Images : LLM {
  suspend fun createImages(request: CreateImageRequest): ImagesResponse

  /**
   * Run a [prompt] describes the images you want to generate within the context of [CoreAIScope].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  suspend fun images(
    prompt: String,
    context: VectorStore,
    numberImages: Int = 1,
    quality: CreateImageRequest.Quality = CreateImageRequest.Quality.standard
  ): ImagesResponse {
    val request =
      CreateImageRequest(
        prompt = prompt,
        n = numberImages,
        quality = quality,
      )
    return createImages(request)
  }
}
