package com.xebia.functional.xef.data

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.infrastructure.BodyProvider
import com.xebia.functional.openai.infrastructure.HttpResponse
import com.xebia.functional.openai.infrastructure.TypedBodyProvider
import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.tokenizer.ModelType
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow

class TestChatModel(
  val modelType: ModelType,
  val responses: Map<String, String> = emptyMap(),
) : ChatApi(), AutoCloseable {

  val chat = ChatApi()

  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override suspend fun createChatCompletion(
    request: CreateChatCompletionRequest
  ): HttpResponse<CreateChatCompletionResponse> {
    requests.add(request)
    return CreateChatCompletionResponse(
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
            content = responses[request.messages.last().contentAsString()] ?: "fake-content",
          ),
          finishReason = CreateChatCompletionResponseChoicesInner.FinishReason.stop,
          index = 0
        )
      ),
      usage = CompletionUsage(0, 0, 0)
    )))
  }

  override suspend fun createChatCompletions(
    request: CreateChatCompletionRequest
  ): Flow<CreateChatCompletionStreamResponse> {
    throw NotImplementedError()
  }

  override fun tokensFromMessages(messages: List<ChatCompletionRequestMessage>): Int {
    return messages.sumOf { it.contentAsString()?.length ?: 0 }
  }

  override suspend fun createEmbeddings(request: CreateEmbeddingRequest): CreateEmbeddingResponse {
    return CreateEmbeddingResponse(
      data = emptyList(),
      model = "",
      `object` = CreateEmbeddingResponse.Object.list,
      usage = CreateEmbeddingResponseUsage(0, 0)
    )
  }
}
