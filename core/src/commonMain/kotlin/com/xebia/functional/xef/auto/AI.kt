package com.xebia.functional.xef.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.recover
import arrow.core.right
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.embeddings.OpenAIEmbeddings
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.vectorstores.CombinedVectorStore
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.LocalVectorStoreBuilder
import com.xebia.functional.xef.vectorstores.VectorStore
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmName
import kotlin.time.ExperimentalTime
import kotlinx.serialization.DeserializationStrategy
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

suspend inline fun <A, reified B> A.chain(crossinline template: (A) -> String): B {
  val input = this
  return ai { prompt<B>(template(input)) }.getOrThrow()
}

/**
 * The [AIScope] is the context in which [AI] values are run. It encapsulates all the dependencies
 * required to run [AI] values, and provides convenient syntax for writing [AI] based programs.
 *
 * It exposes the [ResourceScope] so you can easily add your own resources with the scope of the
 * [AI] program, and [Raise] of [AIError] in case you want to compose any [Raise] based actions.
 */
class AIScope(
  val openAIClient: OpenAIClient,
  val context: VectorStore,
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

  @AiDsl
  suspend fun extendContext(vararg docs: String) {
    context.addTexts(docs.toList())
  }

  /**
   * Creates a new scoped [VectorStore] using [store], which is scoped to the [block] lambda. The
   * [block] also runs on a _nested_ [resourceScope], meaning that all additional resources created
   * within [block] will be finalized after [block] finishes.
   */
  @AiDsl
  suspend fun <A> contextScope(
    store: suspend ResourceScope.(Embeddings) -> VectorStore,
    block: AI<A>
  ): A = resourceScope {
    val newStore = store(this@AIScope.embeddings)
    AIScope(
        this@AIScope.openAIClient,
        CombinedVectorStore(newStore, this@AIScope.context),
        this@AIScope.embeddings,
        this@AIScope.logger,
        this,
        this@AIScope
      )
      .block()
  }

  @AiDsl
  suspend fun <A> contextScope(block: AI<A>): A = contextScope(LocalVectorStoreBuilder, block)

  /** Add new [docs] to the [context], and then executes the [block]. */
  @AiDsl
  @JvmName("contextScopeWithDocs")
  suspend fun <A> contextScope(docs: List<String>, block: AI<A>): A = contextScope {
    extendContext(*docs.toTypedArray())
    block(this)
  }
}
