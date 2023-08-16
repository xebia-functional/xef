package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.serialization.JacksonSerialization
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Images
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher

actual abstract class PlatformConversation
actual constructor(
  store: VectorStore,
  conversationId: ConversationId?,
) : Conversation, AutoClose, AutoCloseable {

  val coroutineScope = CoroutineScope(SupervisorJob())

  fun addContext(docs: List<String>): CompletableFuture<Unit> =
    coroutineScope.addContextAsync(docs).asCompletableFuture()

  fun addContextFromArray(docs: Array<out String>): CompletableFuture<Unit> =
    coroutineScope.addContextAsync(docs.toList()).asCompletableFuture()

  fun <A> prompt(
    chat: ChatWithFunctions,
    prompt: Prompt,
    functions: List<CFunction>,
    serializer: FromJson<A>,
    promptConfiguration: PromptConfiguration
  ): CompletableFuture<A> =
    coroutineScope
      .promptAsync(
        chatWithFunctions = chat,
        prompt = prompt,
        functions = functions,
        serializer = serializer::fromJson,
        promptConfiguration = promptConfiguration,
      )
      .asCompletableFuture()

  @JvmOverloads
  fun <A> prompt(
    chat: ChatWithFunctions,
    prompt: Prompt,
    target: Class<A>,
    functions: List<CFunction> = listOf(generateCFunctionFromClass(target)),
    serializer: FromJson<A> = FromJson { json ->
      JacksonSerialization.objectMapper.readValue(json, target)
    },
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): CompletableFuture<A> =
    coroutineScope
      .promptAsync(
        chatWithFunctions = chat,
        prompt = prompt,
        functions = functions,
        serializer = serializer::fromJson,
        promptConfiguration = promptConfiguration,
      )
      .asCompletableFuture()

  fun <A> generateCFunctionFromClass(target: Class<A>) =
    CFunction(
      name = target.simpleName,
      description = "Generated function for ${target.simpleName}",
      parameters = JacksonSerialization.schemaGenerator.generateSchema(target).toString()
    )

  @JvmOverloads
  fun promptMessage(
    chat: Chat,
    prompt: Prompt,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): CompletableFuture<String> =
    coroutineScope
      .promptMessageAsync(
        chat = chat,
        prompt = prompt,
        promptConfiguration = promptConfiguration,
      )
      .asCompletableFuture()

  @JvmOverloads
  fun promptMessages(
    chat: Chat,
    prompt: Prompt,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): CompletableFuture<List<String>> =
    coroutineScope
      .promptMessagesAsync(
        chat = chat,
        prompt = prompt,
        functions = functions,
        promptConfiguration = promptConfiguration,
      )
      .asCompletableFuture()

  @JvmOverloads
  fun promptStreaming(
    chat: Chat,
    prompt: Prompt,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
    functions: List<CFunction> = emptyList(),
  ): Publisher<String> =
    chat
      .promptStreaming(
        prompt = prompt,
        scope = conversation,
        functions = functions,
        promptConfiguration = promptConfiguration,
      )
      .asPublisher()

  /**
   * Run a [prompt] describes the images you want to generate within the context of [Conversation].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  @AiDsl
  fun images(
    images: Images,
    prompt: Prompt,
    numberImages: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): CompletableFuture<ImagesGenerationResponse> =
    coroutineScope
      .imagesAsync(
        images = images,
        prompt = prompt,
        numberImages = numberImages,
        size = size,
        promptConfiguration = promptConfiguration,
      )
      .asCompletableFuture()

  actual companion object {
    actual fun create(store: VectorStore, conversationId: ConversationId?): PlatformConversation =
      JVMConversation(store, conversationId)
  }

  override fun close() {
    coroutineScope.cancel()
  }
}
