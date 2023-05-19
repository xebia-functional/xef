package com.xebia.functional.xef.agents

import arrow.core.raise.Raise
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.ImagesGenerationRequest
import com.xebia.functional.xef.llm.openai.ImagesGenerationResponse
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.VectorStore

class ImageGenerationAgent(
  private val llm: OpenAIClient,
  private val prompt: Prompt,
  private val context: VectorStore = VectorStore.EMPTY,
  private val user: String = "testing",
  private val numberImages: Int,
  private val size: String,
  private val bringFromContext: Int = 10
) : Agent<ImagesGenerationResponse> {

  override val name = "Image Generation Agent"
  override val description: String = "Generates images"

  override suspend fun Raise<AIError>.call(): ImagesGenerationResponse {
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

    return callImageGenerationEndpoint(promptWithContext)
  }

  private suspend fun callImageGenerationEndpoint(prompt: String): ImagesGenerationResponse {
    val request =
      ImagesGenerationRequest(
        prompt = prompt,
        numberImages = numberImages,
        size = size,
        user = user
      )
    return llm.createImages(request)
  }
}
