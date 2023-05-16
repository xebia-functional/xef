package com.xebia.functional.xef.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.recover
import arrow.core.right
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.agents.ContextualAgent
import com.xebia.functional.xef.agents.DeserializerLLMAgent
import com.xebia.functional.xef.agents.ImageGenerationAgent
import com.xebia.functional.xef.agents.LLMAgent
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.embeddings.OpenAIEmbeddings
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.ImagesGenerationResponse
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.prompt.PromptTemplate
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import kotlin.jvm.JvmName
import kotlin.time.ExperimentalTime
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonObject

@DslMarker annotation class AiDsl

data class SerializationConfig<A>(
  val jsonSchema: JsonObject,
  val descriptor: SerialDescriptor,
  val deserializationStrategy: DeserializationStrategy<A>,
)

/**
 * An [AI] value represents an action relying on artificial intelligence that can be run to produce
 * an [A]. This value is _lazy_ and can be combined with other `AI` values using [AIScope.invoke],
 * and thus forms a monadic DSL.
 *
 * All [AI] actions that are composed together using [AIScope.invoke] share the same [VectorStore],
 * [OpenAIEmbeddings] and [OpenAIClient] instances.
 */
typealias AI<A> = suspend AIScope.() -> A

/** A DSL block that makes it more convenient to construct [AI] values. */
inline fun <A> ai(noinline block: suspend AIScope.() -> A): AI<A> = block

/**
 * Run the [AI] value to produce an [A], this method initialises all the dependencies required to
 * run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 */
suspend inline fun <A> AI<A>.getOrElse(crossinline orElse: suspend (AIError) -> A): A =
  AIScope(this) { orElse(it) }

@OptIn(ExperimentalTime::class)
suspend fun <A> AIScope(block: suspend AIScope.() -> A, orElse: suspend (AIError) -> A): A =
  recover({
    resourceScope {
      val openAIConfig = OpenAIConfig()
      val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
      val logger = KotlinLogging.logger("AutoAI")
      val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
      val vectorStore = LocalVectorStore(embeddings)
      val scope = AIScope(openAiClient, vectorStore, embeddings, logger, this, this@recover)
      block(scope)
    }
  }) {
    orElse(it)
  }

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. this method initialises all the
 * dependencies required to run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @see getOrElse for an operator that allow directly handling the [AIError] case.
 */
suspend inline fun <reified A> AI<A>.toEither(): Either<AIError, A> =
  ai { invoke().right() }.getOrElse { it.left() }

// TODO: Allow traced transformation of Raise errors
class AIException(message: String) : RuntimeException(message)

/**
 * Run the [AI] value to produce [A]. this method initialises all the dependencies required to run
 * the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @throws AIException in case something went wrong.
 * @see getOrElse for an operator that allow directly handling the [AIError] case instead of
 *   throwing.
 */
suspend inline fun <reified A> AI<A>.getOrThrow(): A = getOrElse { throw AIException(it.reason) }

/**
 * The [AIScope] is the context in which [AI] values are run. It encapsulates all the dependencies
 * required to run [AI] values, and provides convenient syntax for writing [AI] based programs.
 *
 * It exposes the [ResourceScope] so you can easily add your own resources with the scope of the
 * [AI] program, and [Raise] of [AIError] in case you want to compose any [Raise] based actions.
 */
class AIScope(
  @PublishedApi internal val openAIClient: OpenAIClient,
  @PublishedApi internal val context: VectorStore,
  internal val embeddings: Embeddings,
  private val logger: KLogger,
  resourceScope: ResourceScope,
  raise: Raise<AIError>,
) : ResourceScope by resourceScope, Raise<AIError> by raise {

  /**
   * Allows invoking [AI] values in the context of this [AIScope].
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
  @AiDsl @JvmName("invokeAI") suspend operator fun <A> AI<A>.invoke(): A = invoke(this@AIScope)

  /** Runs the [agent] to enlarge the [context], and then executes the [scope]. */
  @AiDsl
  suspend fun <A> context(agent: ContextualAgent, scope: suspend AIScope.() -> A): A =
    context(listOf(agent), scope)

  /** Runs the [agents] to enlarge the [context], and then executes the [scope]. */
  @AiDsl
  suspend fun <A> context(agents: Collection<ContextualAgent>, scope: suspend AIScope.() -> A): A {
    agents.forEach {
      logger.debug { "[${it.name}] Running" }
      val docs = with(it) { call() }
      if (docs.isNotEmpty()) {
        context.addTexts(docs)
        logger.debug { "[${it.name}] Found and memorized ${docs.size} docs" }
      } else {
        logger.debug { "[${it.name}] Found no docs" }
      }
    }
    return scope(this)
  }

  /** Add new [docs] to the [context], and then executes the [scope]. */
  @AiDsl
  suspend fun <A> context(docs: List<String>, scope: suspend AIScope.() -> A): A {
    context.addTexts(docs)
    return scope(this)
  }

  @AiDsl
  suspend fun promptMessage(
    question: String,
    model: LLMModel = LLMModel.GPT_3_5_TURBO
  ): List<String> = promptMessage(PromptTemplate(question), emptyMap(), model)

  @AiDsl
  suspend fun promptMessage(
    prompt: PromptTemplate<String>,
    variables: Map<String, String>,
    model: LLMModel = LLMModel.GPT_3_5_TURBO
  ): List<String> = with(LLMAgent(openAIClient, prompt, model, context)) { call(variables) }

  /**
   * Run a [question] describes the task you want to solve within the context of [AIScope]. Returns
   * a value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
   *
   * @throws SerializationException if serializer cannot be created (provided [A] or its type
   *   argument is not serializable).
   * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
   */
  @AiDsl
  suspend inline fun <reified A> prompt(
    question: String,
    model: LLMModel = LLMModel.GPT_3_5_TURBO
  ): A = prompt(PromptTemplate(question), emptyMap(), model)

  /**
   * Run a [prompt] describes the task you want to solve within the context of [AIScope]. Returns a
   * value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
   *
   * @throws SerializationException if serializer cannot be created (provided [A] or its type
   *   argument is not serializable).
   * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
   */
  @AiDsl
  suspend inline fun <reified A> prompt(
    prompt: PromptTemplate<String>,
    variables: Map<String, String>,
    model: LLMModel = LLMModel.GPT_3_5_TURBO
  ): A = with(DeserializerLLMAgent<A>(openAIClient, prompt, model, context)) { call(variables) }

  /**
   * Run a [prompt] describes the images you want to generate within the context of [AIScope].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [PromptTemplate] describing the images you want to generate.
   * @param variables a map of variables to be replaced in the [prompt].
   * @param numberImages number of images to generate.
   * @param imageSize size of the images to generate.
   */
  @AiDsl
  suspend fun images(
    prompt: PromptTemplate<String>,
    variables: Map<String, String>,
    numberImages: Int = 1,
    imageSize: String = "1024x1024"
  ): ImagesGenerationResponse =
    with(
      ImageGenerationAgent(
        llm = openAIClient,
        template = prompt,
        context = context,
        numberImages = numberImages,
        imageSize = imageSize
      )
    ) {
      call(variables)
    }

  /**
   * Run a [prompt] describes the images you want to generate within the context of [AIScope].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [PromptTemplate] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param imageSize size of the images to generate.
   */
  @AiDsl
  suspend fun images(
    prompt: String,
    numberImages: Int = 1,
    imageSize: String = "1024x1024"
  ): ImagesGenerationResponse = images(PromptTemplate(prompt), emptyMap(), numberImages, imageSize)

  /**
   * Run a [prompt] describes the images you want to generate within the context of [AIScope].
   * Produces a [ImagesGenerationResponse] which then gets serialized to [A] through [prompt].
   *
   * @param prompt a [PromptTemplate] describing the images you want to generate.
   * @param n number of images to generate.
   * @param imageSize size of the images to generate.
   */
  @AiDsl
  suspend inline fun <reified A> Raise<AIError>.image(
    prompt: String,
    imageSize: String = "1024x1024",
    llmModel: LLMModel = LLMModel.GPT_3_5_TURBO
  ): A {
    val imageResponse = images(prompt, 1, imageSize)
    val url = imageResponse.data.firstOrNull() ?: raise(AIError.NoResponse)
    return either {
        PromptTemplate(
          """|Instructions: Format this [URL] and [PROMPT] information in the desired JSON response format
           |specified at the end of the message.
           |[URL]: 
           |```
           |{url}
           |```
           |[PROMPT]:
           |```
           |{prompt}
           |```
    """
            .trimMargin(),
          listOf("url", "prompt")
        )
      }
      .fold(
        { raise(AIError.InvalidInputs(it.reason)) },
        { prompt(it, mapOf("url" to url.url, "prompt" to prompt), llmModel) }
      )
  }

  @AiDsl
  suspend fun <A> withContextStore(store: (Embeddings) -> Resource<VectorStore>, block: AI<A>): A =
    AIScope(openAIClient, store(embeddings).bind(), embeddings, logger, this, this).block()
}
