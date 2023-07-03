package com.xebia.functional.xef.auto.llm.openai

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AI
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import kotlin.time.ExperimentalTime

class MockOpenAIClient(
  private val completion: (CompletionRequest) -> CompletionResult = {
    throw NotImplementedError("completion not implemented")
  },
  private val chatCompletion: (ChatCompletionRequest) -> ChatCompletionResponse = {
    throw NotImplementedError("chat completion not implemented")
  },
  private val chatCompletionRequestWithFunctions:
    (ChatCompletionRequestWithFunctions) -> ChatCompletionResponseWithFunctions =
    {
      throw NotImplementedError("chat completion not implemented")
    },
  private val embeddings: (EmbeddingRequest) -> EmbeddingResult = ::nullEmbeddings,
  private val images: (ImagesGenerationRequest) -> ImagesGenerationResponse = {
    throw NotImplementedError("images not implemented")
  },
) : ChatWithFunctions, Images, Completion, Embeddings {

  override val name: String = "mock"
  override val modelType: ModelType = ModelType.GPT_3_5_TURBO

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult =
    completion(request)

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse = chatCompletion(request)

  override suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): ChatCompletionResponseWithFunctions = chatCompletionRequestWithFunctions(request)

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult =
    embeddings(request)

  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse =
    images(request)

  override fun tokensFromMessages(messages: List<Message>): Int = 0

  override fun close() {}
}

fun nullEmbeddings(request: EmbeddingRequest): EmbeddingResult {
  val results = request.input.mapIndexed { index, s -> Embedding(s, listOf(0F), index) }
  return EmbeddingResult(results, Usage.ZERO)
}

fun simpleMockAIClient(execute: (String) -> String): MockOpenAIClient =
  MockOpenAIClient(
    completion = { req ->
      val request = "${req.prompt.orEmpty()} ${req.suffix.orEmpty()}"
      val response = execute(request)
      val result = CompletionChoice(response, 0, null, "end")
      val requestTokens = request.split(' ').size
      val responseTokens = response.split(' ').size
      val usage = Usage(requestTokens, responseTokens, requestTokens + responseTokens)
      CompletionResult("FakeID123", "", 0, req.model, listOf(result), usage)
    },
    chatCompletion = { req ->
      val responses =
        req.messages.mapIndexed { ix, msg ->
          val response = execute(msg.content ?: "")
          Choice(Message(msg.role, response, msg.role.name), "end", ix)
        }
      val requestTokens = req.messages.sumOf { it.content?.split(' ')?.size ?: 0 }
      val responseTokens = responses.sumOf { it.message?.content?.split(' ')?.size ?: 0 }
      val usage = Usage(requestTokens, responseTokens, requestTokens + responseTokens)
      ChatCompletionResponse("FakeID123", "", 0, req.model, usage, responses)
    }
  )

@OptIn(ExperimentalTime::class)
suspend fun <A> MockAIScope(
  mockClient: MockOpenAIClient,
  block: suspend CoreAIScope.() -> A,
  orElse: suspend (AIError) -> A
): A =
  try {
    val embeddings = OpenAIEmbeddings(mockClient)
    val vectorStore = LocalVectorStore(embeddings)
    val scope = CoreAIScope(embeddings, vectorStore)
    block(scope)
  } catch (e: AIError) {
    orElse(e)
  }

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. This method uses the [mockAI] to
 * compute the different responses.
 */
suspend fun <A> AI<A>.mock(mockAI: MockOpenAIClient): Either<AIError, A> =
  MockAIScope(mockAI, { invoke().right() }, { it.left() })

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. This method uses the [mockAI] to
 * compute the different responses.
 */
suspend fun <A> AI<A>.mock(mockAI: (String) -> String): Either<AIError, A> =
  MockAIScope(simpleMockAIClient(mockAI), { invoke().right() }, { it.left() })
