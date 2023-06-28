package com.xebia.functional.xef.auto

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt

/**
 * Run a [prompt] describes the images you want to generate within the context of [CoreAIScope].
 * Produces a [ImagesGenerationResponse] which then gets serialized to [A] through [prompt].
 *
 * @param prompt a [Prompt] describing the images you want to generate.
 * @param size the size of the images to generate.
 */
suspend inline fun <reified A> CoreAIScope.image(
  prompt: String,
  user: String = "testing",
  size: String = "1024x1024",
  bringFromContext: Int = 10
): A {
  val imageResponse = images(prompt, user, 1, size, bringFromContext)
  val url = imageResponse.data.firstOrNull() ?: throw AIError.NoResponse()
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
