package com.xebia.functional.xef.data

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.functions.FunChatCompletionRequest
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.coroutines.flow.Flow

class TestFunctionsModel(
  override val modelType: ModelType,
  val responses: Map<String, String> = emptyMap(),
) : ChatWithFunctions, Embeddings, AutoCloseable {

  var requests: MutableList<FunChatCompletionRequest> = mutableListOf()

  override fun tokensFromMessages(messages: List<Message>): Int {
    return messages.sumOf { it.content.length }
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    return EmbeddingResult(data = emptyList(), usage = Usage.ZERO)
  }

  override suspend fun createChatCompletionWithFunctions(
    request: FunChatCompletionRequest
  ): ChatCompletionResponseWithFunctions {
    requests.add(request)
    val response = responses[request.messages.last().content] ?: "fake-content"

    val assistantResponse =
      MessageWithFunctionCall(
        role = Role.USER.name,
        content = response,
        functionCall = FunctionCall("fake-function-name", response),
        name = Role.USER.name
      )

    val promptTokens = tokensFromMessages(request.messages)
    val totalTokens = promptTokens + tokensFromMessages(listOf(assistantResponse.toMessage()))

    return ChatCompletionResponseWithFunctions(
      id = "fake-id",
      `object` = "fake-object",
      created = 0,
      model = "fake-model",
      choices =
        listOf(
          ChoiceWithFunctions(
            message = assistantResponse,
            finishReason = "fake-finish-reason",
            index = 0
          )
        ),
      usage = Usage(promptTokens, 0, totalTokens)
    )
  }

  override suspend fun createChatCompletionsWithFunctions(
    request: FunChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    TODO("Not yet implemented")
  }
}
