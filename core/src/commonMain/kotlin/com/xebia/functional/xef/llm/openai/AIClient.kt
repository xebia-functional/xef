package com.xebia.functional.xef.llm.openai

import com.xebia.functional.xef.llm.openai.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse

interface AIClient : AutoCloseable {
  suspend fun createCompletion(request: CompletionRequest): CompletionResult

  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

  suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): ChatCompletionResponseWithFunctions

  suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult

  suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse
}
