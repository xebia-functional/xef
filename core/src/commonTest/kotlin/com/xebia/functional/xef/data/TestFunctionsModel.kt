package com.xebia.functional.xef.data

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.coroutines.flow.Flow

class TestFunctionsModel(
  override val modelType: ModelType,
  override val name: String,
  val responses: Map<String, String> = emptyMap(),
) : ChatWithFunctions, Embeddings, AutoCloseable {

  var requests: MutableList<ChatCompletionRequest> = mutableListOf()

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    requests.add(request)
    return ChatCompletionResponse(
      id = "fake-id",
      `object` = "fake-object",
      created = 0,
      model = "fake-model",
      choices =
        listOf(
          Choice(
            message =
              Message(
                role = Role.USER,
                content = responses[request.messages.last().content] ?: "fake-content",
                name = Role.USER.name
              ),
            finishReason = "fake-finish-reason",
            index = 0
          )
        ),
      usage = Usage.ZERO
    )
  }

  override suspend fun createChatCompletions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    throw NotImplementedError()
  }

  override fun tokensFromMessages(messages: List<Message>): Int {
    return messages.sumOf { it.content.length }
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    return EmbeddingResult(data = emptyList(), usage = Usage.ZERO)
  }

  override suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequest
  ): ChatCompletionResponseWithFunctions {
    requests.add(request)
    val response = responses[request.messages.last().content] ?: "fake-content"
    return ChatCompletionResponseWithFunctions(
      id = "fake-id",
      `object` = "fake-object",
      created = 0,
      model = "fake-model",
      choices =
        listOf(
          ChoiceWithFunctions(
            message =
              MessageWithFunctionCall(
                role = Role.USER.name,
                content = response,
                functionCall = FunctionCall("fake-function-name", response),
                name = Role.USER.name
              ),
            finishReason = "fake-finish-reason",
            index = 0
          )
        ),
      usage = Usage.ZERO
    )
  }
}
