package com.xebia.functional.xef.auto

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.ImagesGenerationRequest
import com.xebia.functional.xef.llm.openai.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt

/**
 * Run a [prompt] describes the images you want to generate within the context of [AIScope].
 * Produces a [ImagesGenerationResponse] which then gets serialized to [A] through [prompt].
 *
 * @param prompt a [Prompt] describing the images you want to generate.
 * @param size the size of the images to generate.
 */
suspend inline fun <reified A> AIScope.image(
  prompt: String,
  user: String = "testing",
  size: String = "1024x1024",
  bringFromContext: Int = 10
): A {
  val imageResponse = images(prompt, user, 1, size, bringFromContext)
  val url = imageResponse.data.firstOrNull() ?: raise(AIError.NoResponse)
  return prompt<A>(
    """|Instructions: Format this [URL] and [PROMPT] information in the desired JSON response format
       |specified at the end of the message.
       |[URL]: 
       |```
       |$url
       |```
       |[PROMPT]:
       |```
       |$prompt
       |```"""
      .trimMargin()
  )
}

/**
 * Run a [prompt] describes the images you want to generate within the context of [AIScope]. Returns
 * a [ImagesGenerationResponse] containing time and urls with images generated.
 *
 * @param prompt a [Prompt] describing the images you want to generate.
 * @param numberImages number of images to generate.
 * @param size the size of the images to generate.
 */
suspend fun AIScope.images(
  prompt: String,
  user: String = "testing",
  numberImages: Int = 1,
  size: String = "1024x1024",
  bringFromContext: Int = 10
): ImagesGenerationResponse = images(Prompt(prompt), user, numberImages, size, bringFromContext)

/**
 * Run a [prompt] describes the images you want to generate within the context of [AIScope]. Returns
 * a [ImagesGenerationResponse] containing time and urls with images generated.
 *
 * @param prompt a [Prompt] describing the images you want to generate.
 * @param numberImages number of images to generate.
 * @param size the size of the images to generate.
 */
suspend fun AIScope.images(
  prompt: Prompt,
  user: String = "testing",
  numberImages: Int = 1,
  size: String = "1024x1024",
  bringFromContext: Int = 10
): ImagesGenerationResponse {
  val ctxInfo = context.similaritySearch(prompt.message, bringFromContext)
  val promptWithContext =
    if (ctxInfo.isNotEmpty()) {
      """|Instructions: Use the [Information] below delimited by 3 backticks to accomplish
         |the [Objective] at the end of the prompt.
         |Try to match the data returned in the [Objective] with this [Information] as best as you can.
         |[Information]:
         |```
         |${ctxInfo.joinToString("\n")}
         |```
         |$prompt"""
        .trimMargin()
    } else prompt.message
  val request =
    ImagesGenerationRequest(
      prompt = promptWithContext,
      numberImages = numberImages,
      size = size,
      user = user
    )
  return openAIClient.createImages(request)
}
