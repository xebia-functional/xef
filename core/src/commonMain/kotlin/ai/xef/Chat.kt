package ai.xef

import com.xebia.functional.xef.llm.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*


sealed interface Model {
  val modelName: String
}

interface Embeddings : Model {
  suspend fun createEmbedding(embeddingsRequest: EmbeddingRequest): EmbeddingResponse
}

interface Chat : Model {

  interface Tokenizer {
    fun encode(text: String): List<Int>
    fun truncateText(text: String, maxTokens: Int): String
    fun tokensFromMessages(history: List<ChatCompletionRequestMessage>): Int
  }

  val tokenPaddingSum: Int
  val tokenPadding: Int
  val maxContextLength: Int

  val tokenizer: Tokenizer

  /**
   * Creates a model response for the given chat conversation.
   *
   * @param createChatCompletionRequest
   * @return CreateChatCompletionResponse
   */
  suspend fun createChatCompletion(createChatCompletionRequest: CreateChatCompletionRequest): CreateChatCompletionResponse
  /**
   * Streaming variant: Creates a model response for the given chat conversation.
   * By default, the client is modified to timeout after 60 seconds. Which is overridable by the [configure].
   *
   * @param createChatCompletionRequest
   * @return [Flow]<[CreateChatCompletionStreamResponse]>
   */
  fun createChatCompletionStream(createChatCompletionRequest: CreateChatCompletionRequest): Flow<CreateChatCompletionStreamResponse>

}
