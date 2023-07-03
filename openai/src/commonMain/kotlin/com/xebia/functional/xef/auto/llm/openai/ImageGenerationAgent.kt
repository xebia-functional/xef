package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Images
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
  imageModel: Images,
  serializationModel: ChatWithFunctions,
  prompt: String,
  size: String = "1024x1024",
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A {
  val imageResponse = imageModel.images(prompt, context, 1, size, promptConfiguration)
  val url = imageResponse.data.firstOrNull() ?: throw AIError.NoResponse()
  return prompt<A>(
    serializationModel,
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
