package com.xebia.functional.xef.data

import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Embeddings
import kotlinx.coroutines.flow.Flow

class TestFunctionsModel(
  override val modelType: ModelType,
  val responses: Map<String, String> = emptyMap(),
) : ChatWithFunctions, Embeddings, AutoCloseable {

  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override fun copy(modelType: ModelType) = TestFunctionsModel(modelType, responses)

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

  override suspend fun createChatCompletionWithFunctions(
    request: CreateChatCompletionRequest
  ): CreateChatCompletionResponse {
    requests.add(request)
    val response = responses[request.messages.last().contentAsString()] ?: "fake-content"
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
            content = response,
            toolCalls = listOf(ChatCompletionMessageToolCall("fake-function-name", ChatCompletionMessageToolCall.Type.function, ChatCompletionMessageToolCallFunction(response, response)))
          ),
          finishReason = CreateChatCompletionResponseChoicesInner.FinishReason.functionCall,
          index = 0
        )
      ),
      usage = CompletionUsage(0, 0, 0)
    )
  }

  override suspend fun createChatCompletionsWithFunctions(
    request: CreateChatCompletionRequest
  ): Flow<CreateChatCompletionStreamResponse> {
    TODO("Not yet implemented")
  }
}
