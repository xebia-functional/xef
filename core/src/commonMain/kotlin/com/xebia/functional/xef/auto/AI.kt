package com.xebia.functional.xef.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.embeddings.OpenAIEmbeddings
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient
import com.xebia.functional.xef.llm.openai.OpenAIClient
import com.xebia.functional.xef.llm.openai.simpleMockAIClient
import com.xebia.functional.xef.vectorstores.CombinedVectorStore
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlin.jvm.JvmName
import kotlin.time.ExperimentalTime

@DslMarker annotation class AiDsl

/**
 * An [AI] value represents an action relying on artificial intelligence that can be run to produce
 * an `A`. This value is _lazy_ and can be combined with other `AI` values using [AIScope.invoke],
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

@OptIn(ExperimentalTime::class, ExperimentalStdlibApi::class)
suspend fun <A> AIScope(block: suspend AIScope.() -> A, orElse: suspend (AIError) -> A): A =
  try {
    val openAIConfig = OpenAIConfig()
    KtorOpenAIClient(openAIConfig).use { openAiClient ->
      val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient)
      val vectorStore = LocalVectorStore(embeddings)
      val scope = AIScope(openAiClient, vectorStore, embeddings)
      block(scope)
    }
  } catch (e: AIError) {
    orElse(e)
  }

@OptIn(ExperimentalTime::class)
suspend fun <A> MockAIScope(
  mockAI: (String) -> String,
  block: suspend AIScope.() -> A,
  orElse: suspend (AIError) -> A
): A =
  try {
    val mockClient = simpleMockAIClient(mockAI)
    val embeddings = OpenAIEmbeddings(OpenAIConfig(), mockClient)
    val vectorStore = LocalVectorStore(embeddings)
    val scope = AIScope(mockClient, vectorStore, embeddings)
    block(scope)
  } catch (e: AIError) {
    orElse(e)
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

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. This method uses the [mockAI] to
 * compute the different responses.
 */
suspend fun <A> AI<A>.mock(mockAI: (String) -> String): Either<AIError, A> =
  MockAIScope(mockAI, { invoke().right() }, { it.left() })

/**
 * Run the [AI] value to produce [A]. this method initialises all the dependencies required to run
 * the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @throws AIError in case something went wrong.
 * @see getOrElse for an operator that allow directly handling the [AIError] case instead of
 *   throwing.
 */
suspend inline fun <reified A> AI<A>.getOrThrow(): A = getOrElse { throw it }

/**
 * The [AIScope] is the context in which [AI] values are run. It encapsulates all the dependencies
 * required to run [AI] values, and provides convenient syntax for writing [AI] based programs.
 */
class AIScope(
  val openAIClient: OpenAIClient,
  val context: VectorStore,
  val embeddings: Embeddings
) {

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
   * Creates a nested scope that combines the provided [store] with the outer _store_. This is done
   * using [CombinedVectorStore].
   *
   * **Note:** if the implementation of [VectorStore] is relying on resources you're manually
   * responsible for closing any potential resources.
   */
  @AiDsl
  suspend fun <A> contextScope(store: VectorStore, block: AI<A>): A =
    AIScope(
        this@AIScope.openAIClient,
        CombinedVectorStore(store, this@AIScope.context),
        this@AIScope.embeddings
      )
      .block()

  @AiDsl
  suspend fun <A> contextScope(block: AI<A>): A = contextScope(LocalVectorStore(embeddings), block)

  /** Add new [docs] to the [context], and then executes the [block]. */
  @AiDsl
  @JvmName("contextScopeWithDocs")
  suspend fun <A> contextScope(docs: List<String>, block: AI<A>): A = contextScope {
    extendContext(*docs.toTypedArray())
    block(this)
  }
}
