package com.xebia.functional.xef.llm.openai

import com.xebia.functional.xef.llm.openai.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse

class MockOpenAIClient(
  private val completion: (CompletionRequest) -> CompletionResult = {
    throw NotImplementedError("completion not implemented")
  },
  private val chatCompletion: (ChatCompletionRequest) -> ChatCompletionResponse = {
    throw NotImplementedError("chat completion not implemented")
  },
  private val chatCompletionRequestWithFunctions: (ChatCompletionRequestWithFunctions) -> ChatCompletionResponseWithFunctions = {
    throw NotImplementedError("chat completion not implemented")
  },
  private val embeddings: (EmbeddingRequest) -> EmbeddingResult = ::nullEmbeddings,
  private val images: (ImagesGenerationRequest) -> ImagesGenerationResponse = {
    throw NotImplementedError("images not implemented")
  },
) : OpenAIClient {
  override suspend fun createCompletion(request: CompletionRequest): CompletionResult =
    completion(request)

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse = chatCompletion(request)

  override suspend fun createChatCompletionWithFunctions(request: ChatCompletionRequestWithFunctions): ChatCompletionResponseWithFunctions =
    chatCompletionRequestWithFunctions(request)

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult =
    embeddings(request)

  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse =
    images(request)
}

fun nullEmbeddings(request: EmbeddingRequest): EmbeddingResult {
  val results = request.input.mapIndexed { index, s -> Embedding(s, listOf(0F), index) }
  return EmbeddingResult(request.model, "", results, Usage.ZERO)
}

fun simpleMockAIClient(execute: (String) -> String): MockOpenAIClient =
  MockOpenAIClient(
    completion = { req ->
      val request = "${req.prompt.orEmpty()} ${req.suffix.orEmpty()}"
      val response = execute(request)
      val result = CompletionChoice(response, 0, null, "end")
      val requestTokens = request.split(' ').size.toLong()
      val responseTokens = response.split(' ').size.toLong()
      val usage = Usage(requestTokens, responseTokens, requestTokens + responseTokens)
      CompletionResult("FakeID123", "", 0, req.model, listOf(result), usage)
    },
    chatCompletion = { req ->
      val responses =
        req.messages.mapIndexed { ix, msg ->
          val response = execute(msg.content)
          Choice(Message(msg.role, response), "end", ix)
        }
      val requestTokens = req.messages.sumOf { it.content.split(' ').size.toLong() }
      val responseTokens = responses.sumOf { it.message.content.split(' ').size.toLong() }
      val usage = Usage(requestTokens, responseTokens, requestTokens + responseTokens)
      ChatCompletionResponse("FakeID123", "", 0, req.model, usage, responses)
    }
  )
