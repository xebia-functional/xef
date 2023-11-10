package com.xebia.functional.xef.conversation

import com.xebia.functional.openai.models.CreateImageRequest
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ImagesResponse
import com.xebia.functional.xef.conversation.serialization.JacksonSerialization
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Images
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.VectorStore
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.reactivestreams.Publisher

actual abstract class PlatformConversation
actual constructor(
  store: VectorStore,
  conversationId: ConversationId?,
) : Conversation, AutoClose, AutoCloseable {

  val coroutineScope = CoroutineScope(SupervisorJob())

  fun addContext(docs: List<String>): CompletableFuture<Unit> =
    coroutineScope.async { super.addContext(docs) }.asCompletableFuture()

  fun addContextFromArray(docs: Array<out String>): CompletableFuture<Unit> =
    coroutineScope.async { super.addContext(docs.toList()) }.asCompletableFuture()

  fun <A> prompt(
    chat: ChatWithFunctions,
    prompt: Prompt,
    function: FunctionObject,
    serializer: FromJson<A>
  ): CompletableFuture<A> =
    coroutineScope
      .async { chat.prompt(prompt, this@PlatformConversation, function, serializer::fromJson) }
      .asCompletableFuture()

  @JvmOverloads
  fun <A> prompt(
    chat: ChatWithFunctions,
    prompt: Prompt,
    target: Class<A>,
    serializer: FromJson<A> = FromJson { json ->
      JacksonSerialization.objectMapper.readValue(json, target)
    }
  ): CompletableFuture<A> =
    coroutineScope
      .async {
        chat.prompt(prompt, this@PlatformConversation, chatFunction(target), serializer::fromJson)
      }
      .asCompletableFuture()

  fun chatFunction(target: Class<*>): FunctionObject {
    val targetString = JacksonSerialization.schemaGenerator.generateSchema(target).toString()
    return FunctionObject(
      name = target.simpleName,
      description = "Generated function for ${target.simpleName}",
      parameters = Json.parseToJsonElement(targetString).jsonObject
    )
  }

  fun promptMessage(chat: Chat, prompt: Prompt): CompletableFuture<String> =
    coroutineScope
      .async { chat.promptMessage(prompt, this@PlatformConversation) }
      .asCompletableFuture()

  fun promptMessages(chat: Chat, prompt: Prompt): CompletableFuture<List<String>> =
    coroutineScope
      .async { chat.promptMessages(prompt, this@PlatformConversation) }
      .asCompletableFuture()

  fun promptStreamingToPublisher(chat: Chat, prompt: Prompt): Publisher<String> =
    chat.promptStreaming(prompt = prompt, scope = conversation).asPublisher()

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
    prompt: String,
    numberImages: Int = 1,
    quality: CreateImageRequest.Quality = CreateImageRequest.Quality.standard
  ): CompletableFuture<ImagesResponse> =
    coroutineScope
      .async { images.images(prompt = prompt, numberImages = numberImages, quality = quality) }
      .asCompletableFuture()

  actual companion object {
    actual fun create(
      store: VectorStore,
      metric: Metric,
      conversationId: ConversationId?
    ): PlatformConversation {
      conversationId?.let { cid -> store.updateIndexByConversationId(cid) }
      return JVMConversation(store, metric, conversationId)
    }
  }

  override fun close() {
    coroutineScope.cancel()
  }
}
