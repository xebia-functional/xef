package com.xebia.functional.xef.data

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.prompt.contentAsString
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow

class TestChatApi(private val responses: Map<String, String> = emptyMap()) : Chat, AutoCloseable {

  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override suspend fun createChatCompletion(
    createChatCompletionRequest: CreateChatCompletionRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CreateChatCompletionResponse {
    requests.add(createChatCompletionRequest)
    val response =
      CreateChatCompletionResponse(
        id = "fake-id",
        `object` = CreateChatCompletionResponse.Object.chat_completion,
        created = 0,
        model = "fake-model",
        choices =
          listOf(
            CreateChatCompletionResponseChoicesInner(
              message =
                ChatCompletionResponseMessage(
                  role = ChatCompletionResponseMessage.Role.assistant,
                  content =
                    responses[createChatCompletionRequest.messages.last().contentAsString()]
                      ?: "fake-content",
                  toolCalls =
                    listOf(
                      ChatCompletionMessageToolCall(
                        id = "fake-tool-id",
                        type = ChatCompletionMessageToolCall.Type.function,
                        function =
                          ChatCompletionMessageToolCallFunction(
                            "fake-function-name",
                            """{ "bar": "fake-answer" }"""
                          )
                      )
                    )
                ),
              finishReason = CreateChatCompletionResponseChoicesInner.FinishReason.stop,
              index = 0,
              logprobs = null
            )
          ),
        usage = CompletionUsage(0, 0, 0)
      )
    return response
  }

  override fun createChatCompletionStream(
    createChatCompletionRequest: CreateChatCompletionRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<CreateChatCompletionStreamResponse> {
    throw NotImplementedError("Not implemented")
  }

  override fun close() {}
}
