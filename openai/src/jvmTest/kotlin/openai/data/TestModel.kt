package openai.data

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.coroutines.flow.Flow

class TestModel(
  override val modelType: ModelType,
  override val name: String,
  val responses: Map<String, String> = emptyMap(),
) : Chat, Embeddings, AutoCloseable {

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
}
