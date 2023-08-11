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
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlinx.coroutines.flow.Flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

/**
 * The [Conversation] is the context in which [AI] values are run. It encapsulates all the
 * dependencies required to run [AI] values, and provides convenient syntax for writing [AI] based
 * programs.
 */
class Conversation
@JvmOverloads
constructor(
  val store: VectorStore,
  val conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
) : AutoCloseable, AutoClose by autoClose() {

  @AiDsl
  suspend fun addContext(vararg docs: String) {
    store.addTexts(docs.toList())
  }

  @AiDsl
  suspend fun addContext(docs: Iterable<String>) {
    store.addTexts(docs.toList())
  }

  @AiDsl
  @JvmName("promptWithSerializer")
  suspend fun <A> ChatWithFunctions.prompt(
    prompt: String,
    functions: List<CFunction>,
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): A {
    return prompt(
      prompt = Prompt(prompt),
      scope = this@Conversation,
      serializer = serializer,
      functions = functions,
      promptConfiguration = promptConfiguration,
    )
  }

  @AiDsl
  suspend fun Chat.promptMessage(
    question: String,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): String =
    promptMessages(question, this@Conversation, emptyList(), promptConfiguration).firstOrNull()
      ?: throw AIError.NoResponse()

  @AiDsl
  suspend fun Chat.promptMessages(
    question: String,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): List<String> =
    promptMessages(Prompt(question), this@Conversation, functions, promptConfiguration)

  @AiDsl
  fun Chat.promptStreaming(
    question: String,
    functions: List<CFunction>,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): Flow<String> =
    promptStreaming(Prompt(question), this@Conversation, functions, promptConfiguration)

  /**
   * Run a [prompt] describes the images you want to generate within the context of [Conversation].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  suspend fun Images.images(
    prompt: String,
    numberImages: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): ImagesGenerationResponse = this.images(Prompt(prompt), numberImages, size, promptConfiguration)

  /**
   * Run a [prompt] describes the images you want to generate within the context of [Conversation].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  suspend fun Images.images(
    prompt: Prompt,
    numberImages: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
  ): ImagesGenerationResponse = images(prompt, store, numberImages, size, promptConfiguration)
}
