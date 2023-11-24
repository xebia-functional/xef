package com.xebia.functional.xef.data

import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.functions.FunChatCompletionRequest
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.coroutines.flow.Flow

class TestFunctionsModel(
  override val contextLength: MaxIoContextLength = MaxIoContextLength.Combined(Int.MAX_VALUE),
  val responses: Map<String, String> = emptyMap(),
) : ChatWithFunctions, Embeddings, AutoCloseable {

  override val modelID = ModelID("test-model")

  var requests: MutableList<FunChatCompletionRequest> = mutableListOf()

  override fun copy(modelID: ModelID) = TestFunctionsModel(contextLength, responses)

  override fun countTokens(text: String): Int = text.length

  override fun truncateText(text: String, maxTokens: Int): String = text

  override fun tokensFromMessages(messages: List<Message>): Int =
    messages.sumOf { it.content.length }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    return EmbeddingResult(data = emptyList(), usage = Usage.ZERO)
  }

  override suspend fun createChatCompletionWithFunctions(
    request: FunChatCompletionRequest
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

  override suspend fun createChatCompletionsWithFunctions(
    request: FunChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    TODO("Not yet implemented")
  }
}
