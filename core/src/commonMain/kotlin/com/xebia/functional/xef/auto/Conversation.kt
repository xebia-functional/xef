package com.xebia.functional.xef.auto

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Images
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlin.jvm.JvmSynthetic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

interface Conversation : AutoClose, AutoCloseable {

  val store: VectorStore

  val conversationId: ConversationId?

  val conversation: Conversation

  @AiDsl
  @JvmSynthetic
  suspend fun addContext(vararg docs: String) {
    store.addTexts(docs.toList())
  }

  fun CoroutineScope.addContextAsync(vararg docs: String): Deferred<Unit> = async {
    store.addTexts(docs.toList())
  }

  fun CoroutineScope.addContextAsync(docs: Iterable<String>): Deferred<Unit> = async {
    store.addTexts(docs.toList())
  }

  @AiDsl
  @JvmSynthetic
  suspend fun addContext(docs: Iterable<String>): Unit {
    store.addTexts(docs.toList())
  }

  @AiDsl
  @JvmSynthetic
  suspend fun <A> ChatWithFunctions.prompt(
    prompt: Prompt,
    functions: List<CFunction>,
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): A {
    return prompt(
      prompt,
      conversation,
      serializer,
      functions,
      promptConfiguration,
    )
  }

  fun <A> CoroutineScope.promptAsync(
    chatWithFunctions: ChatWithFunctions,
    prompt: Prompt,
    functions: List<CFunction>,
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): Deferred<A> = async {
    chatWithFunctions.prompt(
      prompt,
      conversation,
      serializer,
      functions,
      promptConfiguration,
    )
  }

  @AiDsl
  @JvmSynthetic
  suspend fun Chat.promptMessage(
    prompt: Prompt,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): String =
    promptMessages(prompt, conversation, emptyList(), promptConfiguration).firstOrNull()
      ?: throw AIError.NoResponse()

  @AiDsl
  fun CoroutineScope.promptMessageAsync(
    chat: Chat,
    prompt: Prompt,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Deferred<String> = async {
    chat.promptMessages(prompt, conversation, emptyList(), promptConfiguration).firstOrNull()
      ?: throw AIError.NoResponse()
  }

  @AiDsl
  @JvmSynthetic
  suspend fun Chat.promptMessages(
    prompt: Prompt,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> = promptMessages(prompt, conversation, functions, promptConfiguration)

  fun CoroutineScope.promptMessagesAsync(
    chat: Chat,
    prompt: Prompt,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Deferred<List<String>> = async {
    chat.promptMessages(prompt, conversation, functions, promptConfiguration)
  }

  @AiDsl
  fun Chat.promptStreaming(
    prompt: Prompt,
    functions: List<CFunction>,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> = promptStreaming(prompt, conversation, functions, promptConfiguration)

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
  suspend fun Images.images(
    prompt: Prompt,
    numberImages: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): ImagesGenerationResponse = images(prompt, store, numberImages, size, promptConfiguration)

  fun CoroutineScope.imagesAsync(
    images: Images,
    prompt: Prompt,
    numberImages: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Deferred<ImagesGenerationResponse> = async {
    images.images(prompt, store, numberImages, size, promptConfiguration)
  }

  companion object {

    operator fun invoke(
      store: VectorStore,
      conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
    ): PlatformConversation = PlatformConversation.create(store, conversationId)

    @JvmSynthetic
    suspend operator fun <A> invoke(
      store: VectorStore,
      conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString()),
      block: suspend PlatformConversation.() -> A
    ): A = block(invoke(store, conversationId))
  }
}
