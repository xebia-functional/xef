package com.xebia.functional.xef.data

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.infrastructure.BodyProvider
import com.xebia.functional.openai.infrastructure.HttpResponse
import com.xebia.functional.openai.models.*
import com.xebia.functional.tokenizer.ModelType
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import kotlin.coroutines.CoroutineContext

class TestChatModel(val responses: Map<String, String> = emptyMap()) : ChatApi(), AutoCloseable {

  data class TestBodyProvider<T: Any>(val value: T): BodyProvider<T> {
    override suspend fun body(response: io.ktor.client.statement.HttpResponse): T = value
    @Suppress("UNCHECKED_CAST")
    override suspend fun <V : Any> typedBody(response: io.ktor.client.statement.HttpResponse, type: TypeInfo): V = value as V
  }

  class TestHttpResponse(statusCode: Int): io.ktor.client.statement.HttpResponse() {
    override val call: HttpClientCall = TODO()
    @InternalAPI
    override val content: ByteReadChannel = TODO()
    override val coroutineContext: CoroutineContext = TODO()
    override val headers: Headers = Headers.Empty
    override val requestTime: GMTDate = TODO()
    override val responseTime: GMTDate = TODO()
    override val status: HttpStatusCode = HttpStatusCode(statusCode, "Mocked status")
    override val version: HttpProtocolVersion = TODO()
  }

  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override suspend fun createChatCompletion(
    createChatCompletionRequest: CreateChatCompletionRequest
  ): HttpResponse<CreateChatCompletionResponse> {
    requests.add(createChatCompletionRequest)
    val response = CreateChatCompletionResponse(
      id = "fake-id",
      `object` = CreateChatCompletionResponse.Object.chatPeriodCompletion,
      created = 0,
      model = "fake-model",
      choices =
      listOf(
        CreateChatCompletionResponseChoicesInner(
          message =
          ChatCompletionResponseMessage(
            role = ChatCompletionResponseMessage.Role.assistant,
            content = responses[createChatCompletionRequest.messages.last().contentAsString()] ?: "fake-content",
          ),
          finishReason = CreateChatCompletionResponseChoicesInner.FinishReason.stop,
          index = 0
        )
      ),
      usage = CompletionUsage(0, 0, 0)
    )
    return HttpResponse(TestHttpResponse(200), TestBodyProvider(response))
  }

  override fun close() { }

//  override suspend fun createChatCompletions(
//    request: CreateChatCompletionRequest
//  ): Flow<CreateChatCompletionStreamResponse> {
//    throw NotImplementedError()
//  }
//
//  override fun tokensFromMessages(messages: List<ChatCompletionRequestMessage>): Int {
//    return messages.sumOf { it.contentAsString()?.length ?: 0 }
//  }
//
//  override suspend fun createEmbeddings(request: CreateEmbeddingRequest): CreateEmbeddingResponse {
//    return CreateEmbeddingResponse(
//      data = emptyList(),
//      model = "",
//      `object` = CreateEmbeddingResponse.Object.list,
//      usage = CreateEmbeddingResponseUsage(0, 0)
//    )
//  }
}
