package com.xebia.functional.xef.data

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.infrastructure.HttpResponse
import com.xebia.functional.openai.models.*
import com.xebia.functional.xef.utils.TestBodyProvider
import com.xebia.functional.xef.utils.TestHttpResponse
import kotlin.coroutines.CoroutineContext

class TestChatApi(
  private val context: CoroutineContext,
  private val responses: Map<String, String> = emptyMap()
) : ChatApi(), AutoCloseable {

  var requests: MutableList<CreateChatCompletionRequest> = mutableListOf()

  override suspend fun createChatCompletion(
    createChatCompletionRequest: CreateChatCompletionRequest
  ): HttpResponse<CreateChatCompletionResponse> {
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
                  content = responses[createChatCompletionRequest.messages.last().contentAsString()]
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
              index = 0
            )
          ),
        usage = CompletionUsage(0, 0, 0)
      )
    return HttpResponse(TestHttpResponse(context, 200), TestBodyProvider(response))
  }

  override fun close() {}
}
