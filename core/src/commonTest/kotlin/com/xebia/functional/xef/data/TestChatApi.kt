package com.xebia.functional.xef.data

import com.xebia.functional.xef.prompt.contentAsString
import io.github.nomisrev.openapi.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow

class TestChatApi(private val responses: Map<String, String> = emptyMap()) : Chat {

  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override val completions: Chat.Completions =
    object : Chat.Completions {
      override suspend fun createChatCompletion(
        body: CreateChatCompletionRequest,
        configure: HttpRequestBuilder.() -> Unit
      ): CreateChatCompletionResponse {
        requests.add(body)
        val response =
          CreateChatCompletionResponse(
            id = "fake-id",
            `object` = CreateChatCompletionResponse.Object.ChatCompletion,
            created = 0,
            model = "fake-model",
            choices =
              listOf(
                CreateChatCompletionResponse.Choices(
                  message =
                    ChatCompletionResponseMessage(
                      role = ChatCompletionResponseMessage.Role.Assistant,
                      content = responses[body.messages.last().contentAsString()] ?: "fake-content",
                      toolCalls =
                        listOf(
                          ChatCompletionMessageToolCall(
                            id = "fake-tool-id",
                            type = ChatCompletionMessageToolCall.Type.Function,
                            function =
                              ChatCompletionMessageToolCall.Function(
                                "fake-function-name",
                                """{ "bar": "fake-answer" }"""
                              )
                          )
                        )
                    ),
                  finishReason = CreateChatCompletionResponse.Choices.FinishReason.Stop,
                  index = 0,
                  logprobs = null
                )
              ),
            usage = CompletionUsage(0, 0, 0)
          )
        return response
      }

      override suspend fun createChatCompletionStream(
        body: CreateChatCompletionRequest,
        configure: HttpRequestBuilder.() -> Unit
      ): Flow<CreateChatCompletionStreamResponse> {
        TODO("Not yet implemented")
      }
    }
}
