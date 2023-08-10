package com.xebia.functional.xef.auto

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Images
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.CombinedVectorStore
import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.LocalVectorStore
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
  val embeddings: Embeddings,
  val store: VectorStore = LocalVectorStore(embeddings),
  val conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
) : AutoCloseable, AutoClose by autoClose() {

  /**
   * Allows invoking [AI] values in the context of this [Conversation].
   *
   * ```kotlin
   * data class CovidNews(val title: String, val content: String)
   * val covidNewsToday = ai {
   *   val now = LocalDateTime.now()
   *   agent(search("$now covid-19 News")) {
   *     prompt<CovidNews>("write a paragraph of about 300 words about the latest news on covid-19 on $now")
   *   }
   * }
   *
   * data class BreakingNews(val title: String, val content: String, val date: String)
   *
   * fun breakingNews(date: LocalDateTime): AI<BreakingNews> = ai {
   *   agent(search("$date Breaking News")) {
   *     prompt("Summarize all breaking news that happened on ${now.minusDays(it)} in about 300 words")
   *   }
   * }
   *
   * suspend fun AIScope.breakingNewsLastWeek(): List<BreakingNews> {
   *   val now = LocalDateTime.now()
   *   return (0..7).parMap { breakingNews(now.minusDays(it)).invoke() }
   * }
   *
   * fun news(): AI<List<News>> = ai {
   *   val covidNews = parZip(
   *     { covidNewsToday() },
   *     { breakingNewsLastWeek() }
   *   ) { covidNews, breakingNews -> listOf(covidNews) + breakingNews }
   * }
   * ```
   */
  @AiDsl @JvmName("invokeAI") suspend operator fun <A> AI<A>.invoke(): A = invoke(this@Conversation)

  @AiDsl
  suspend fun extendContext(vararg docs: String) {
    store.addTexts(docs.toList())
  }

  /**
   * Creates a nested scope that combines the provided [store] with the outer _store_. This is done
   * using [CombinedVectorStore].
   *
   * **Note:** if the implementation of [VectorStore] is relying on resources you're manually
   * responsible for closing any potential resources.
   */
  @AiDsl
  suspend fun <A> contextScope(store: VectorStore, block: AI<A>): A =
    Conversation(
        this@Conversation.embeddings,
        CombinedVectorStore(store, this@Conversation.store),
      )
      .block()

  @AiDsl
  suspend fun <A> contextScope(block: AI<A>): A = contextScope(LocalVectorStore(embeddings), block)

  /** Add new [docs] to the [store], and then executes the [block]. */
  @AiDsl
  @JvmName("contextScopeWithDocs")
  suspend fun <A> contextScope(docs: List<String>, block: AI<A>): A = contextScope {
    extendContext(*docs.toTypedArray())
    block(this)
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
