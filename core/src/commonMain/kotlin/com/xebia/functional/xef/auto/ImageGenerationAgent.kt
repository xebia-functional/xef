@file:JvmMultifileClass
@file:JvmName("Agent")

package com.xebia.functional.xef.auto

import com.xebia.functional.xef.llm.openai.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

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
