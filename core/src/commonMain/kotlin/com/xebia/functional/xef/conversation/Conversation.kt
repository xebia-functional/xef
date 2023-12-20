package com.xebia.functional.xef.conversation

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.apis.ImagesApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.openai.models.CreateImageRequest
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ImagesResponse
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class Conversation
@JvmOverloads
constructor(
  val store: VectorStore = LocalVectorStore(fromEnvironment { baseUrl -> EmbeddingsApi(baseUrl) }),
  val metric: Metric = Metric.EMPTY,
  val conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
) {

  @AiDsl
  @JvmSynthetic
  suspend fun addContext(vararg docs: String) {
    store.addTexts(docs.toList())
  }

  @AiDsl
  @JvmSynthetic
  suspend fun addContext(docs: Iterable<String>): Unit {
    store.addTexts(docs.toList())
  }

  @AiDsl
  @JvmSynthetic
  suspend inline fun <reified A> prompt(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    chat: ChatApi = fromEnvironment { baseUrl -> ChatApi(baseUrl) },
  ): A = chat.prompt(prompt, this@Conversation, serializer())

  @AiDsl
  @JvmSynthetic
  suspend inline fun promptMessage(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    chat: ChatApi = fromEnvironment { baseUrl -> ChatApi(baseUrl) },
  ): String = chat.promptMessage(prompt, this@Conversation)

  @AiDsl
  @JvmSynthetic
  suspend fun <A> ChatApi.prompt(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    serializer: KSerializer<A>
  ): A = prompt(prompt, this@Conversation, serializer)

  @AiDsl
  @JvmSynthetic
  suspend fun <A> ChatApi.prompt(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    function: FunctionObject,
    serializer: (String) -> A
  ): A = prompt(prompt, this@Conversation, function, serializer)

  @AiDsl
  @JvmSynthetic
  suspend fun ChatApi.promptMessage(
    prompt: Prompt<CreateChatCompletionRequestModel>,
  ): String = promptMessages(prompt, this@Conversation).firstOrNull() ?: throw AIError.NoResponse()

  @AiDsl
  @JvmSynthetic
  suspend fun ChatApi.promptMessages(
    prompt: Prompt<CreateChatCompletionRequestModel>
  ): List<String> = promptMessages(prompt, this@Conversation)

  @AiDsl
  inline fun <reified A> promptStreamingFunctions(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    chat: ChatApi = fromEnvironment(::ChatApi)
  ): Flow<StreamedFunction<A>> = chat.promptStreaming(prompt, this@Conversation, serializer())

  @AiDsl
  fun promptStreaming(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    chat: ChatApi = fromEnvironment(::ChatApi)
  ): Flow<String> = chat.promptStreaming(prompt, this@Conversation)

  @AiDsl
  fun ChatApi.promptStreaming(
    prompt: Prompt<CreateChatCompletionRequestModel>
  ): Flow<String> = promptStreaming(prompt, this@Conversation)

  /**
   * Run a [prompt] describes the images you want to generate within the context of [Conversation].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  @AiDsl
  @JvmSynthetic
  suspend fun ImagesApi.images(
    prompt: String,
    numberImages: Int = 1,
    quality: CreateImageRequest.Quality = CreateImageRequest.Quality.standard
  ): ImagesResponse = images(prompt, numberImages, quality)

  companion object {

    @JvmSynthetic
    suspend operator fun <A> invoke(
      store: VectorStore = LocalVectorStore(fromEnvironment { baseUrl -> EmbeddingsApi(baseUrl) }),
      metric: Metric = Metric.EMPTY,
      conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString()),
      block: suspend Conversation.() -> A
    ): A = block(Conversation(store, metric, conversationId))
  }
}
