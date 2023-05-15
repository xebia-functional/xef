package com.xebia.functional.agents

import arrow.core.raise.Raise
import com.xebia.functional.AIError
import com.xebia.functional.llm.openai.ImagesGenerationRequest
import com.xebia.functional.llm.openai.ImagesGenerationResponse
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import com.xebia.functional.vectorstores.VectorStore

class ImageGenerationAgent(
  private val llm: OpenAIClient,
  private val template: PromptTemplate<String>,
  private val context: VectorStore = VectorStore.EMPTY,
  private val user: String = "testing",
  private val n: Int,
  private val size: String,
  private val bringFromContext: Int = 10
) : Agent<Map<String, String>, ImagesGenerationResponse> {

  override val name = "Image Generation Agent"
  override val description: String = "Generates images"

  override suspend fun Raise<AIError>.call(input: Map<String, String>): ImagesGenerationResponse {
    val prompt = template.format(checkInput(template, input))

    val ctxInfo = context.similaritySearch(prompt, bringFromContext)
    val promptWithContext =
      if (ctxInfo.isNotEmpty())
        """
                |Instructions: Use the [Information] below delimited by 3 backticks to accomplish
                |the [Objective] at the end of the prompt.
                |Try to match the data returned in the [Objective] with this [Information] as best as you can.
                |[Information]:
                |```
                |${ctxInfo.joinToString("\n")}
                |```
                |$prompt
                """
          .trimMargin()
      else prompt

    return callImageGenerationEndpoint(promptWithContext)
  }

  private suspend fun callImageGenerationEndpoint(prompt: String): ImagesGenerationResponse {
    val request = ImagesGenerationRequest(prompt = prompt, n = n, size = size, user = user)
    return llm.createImages(request)
  }
}
