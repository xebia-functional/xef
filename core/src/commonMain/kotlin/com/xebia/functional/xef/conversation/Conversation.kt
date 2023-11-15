package com.xebia.functional.xef.conversation

import com.xebia.functional.openai.apis.ChatApi
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
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmSynthetic
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

interface Conversation {

  val store: VectorStore

  val metric: Metric

  val conversationId: ConversationId?

  val conversation: Conversation

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
  suspend fun <A> ChatApi.prompt(prompt: Prompt<CreateChatCompletionRequestModel>, serializer: KSerializer<A>): A =
    prompt(prompt, conversation, serializer)

  @AiDsl
  @JvmSynthetic
  suspend fun <A> ChatApi.prompt(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    function: FunctionObject,
    serializer: (String) -> A
  ): A = prompt(prompt, conversation, function, serializer)

  @AiDsl
  @JvmSynthetic
  suspend fun ChatApi.promptMessage(
    prompt: Prompt<CreateChatCompletionRequestModel>,
  ): String = promptMessages(prompt, conversation).firstOrNull() ?: throw AIError.NoResponse()

  @AiDsl
  @JvmSynthetic
  suspend fun ChatApi.promptMessages(prompt: Prompt<CreateChatCompletionRequestModel>): List<String> =
    promptMessages(prompt, conversation)

  @AiDsl
  fun ChatApi.promptStreaming(prompt: Prompt<CreateChatCompletionRequestModel>): Flow<String> = promptStreaming(prompt, conversation)

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

    class Default(
      override val store: VectorStore,
      override val metric: Metric,
      override val conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString()),
    ) : Conversation {
      override val conversation: Conversation = this
    }

    operator fun invoke(
      store: VectorStore,
      metric: Metric,
      conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
    ): Conversation = Default(store, metric, conversationId)

    @JvmSynthetic
    suspend operator fun <A> invoke(
      store: VectorStore,
      metric: Metric,
      conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString()),
      block: suspend Conversation.() -> A
    ): A = block(invoke(store, metric, conversationId))
  }
}
