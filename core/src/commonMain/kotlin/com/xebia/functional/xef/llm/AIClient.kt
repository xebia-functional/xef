package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

interface AIClient : AutoCloseable {

  interface Completion : AIClient {
    suspend fun createCompletion(request: CompletionRequest): CompletionResult
  }

  interface Chat : AIClient {
    suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse
    fun tokensFromMessages(messages: List<Message>): Int
  }

  interface ChatWithFunctions : Chat {

    suspend fun createChatCompletionWithFunctions(
      request: ChatCompletionRequestWithFunctions
    ): ChatCompletionResponseWithFunctions

  }

  interface Embeddings : AIClient {

    suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult

  }

  interface Images {

    suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse

  }

  override fun close() {}
}
