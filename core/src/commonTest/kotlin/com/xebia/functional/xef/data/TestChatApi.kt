package com.xebia.functional.xef.data

import ai.xef.Chat
import com.xebia.functional.xef.llm.*
import kotlinx.coroutines.flow.Flow

class TestChatApi(private val responses: Map<String, String> = emptyMap()) : Chat, AutoCloseable {

  override val modelName: String = "fake-model"
  override val tokenPaddingSum: Int = 0
  override val tokenPadding: Int = 0
  override val maxContextLength: Int = 0
  override val tokenizer: Chat.Tokenizer = TestTokenizer()
  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override suspend fun createChatCompletion(
    createChatCompletionRequest: CreateChatCompletionRequest
  ): CreateChatCompletionResponse {
    requests.add(createChatCompletionRequest)
    val response =
      CreateChatCompletionResponse(
        id = "fake-id",
        created = 0,
        model = "fake-model",
        choices =
          listOf(
            CreateChatCompletionResponseChoicesInner(
              message =
                ChatCompletionResponseMessage(
                  role = Role.assistant,
                  content =
                    responses[createChatCompletionRequest.messages.last().content]
                      ?: "fake-content",
                  toolCalls =
                    listOf(
                      ChatCompletionMessageToolCall(
                        id = "fake-tool-id",
                        function =
                          ToolCall(
                            "fake-function-name",
                            """{ "bar": "fake-answer" }"""
                          )
                      )
                    )
                ),
              finishReason = FinishReason.stop
            )
          ),
        usage = Usage(0, 0, 0)
      )
    return response
  }

  override fun createChatCompletionStream(
    createChatCompletionRequest: CreateChatCompletionRequest
  ): Flow<CreateChatCompletionStreamResponse> {
    throw NotImplementedError("Not implemented")
  }

  override fun close() {}
}
