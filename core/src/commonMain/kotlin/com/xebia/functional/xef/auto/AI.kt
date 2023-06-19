package com.xebia.functional.xef.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.embeddings.OpenAIEmbeddings
import com.xebia.functional.xef.env.OpenAIConfig
import com.xebia.functional.xef.llm.openai.*
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
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
suspend fun <A> AIScope(block: AI<A>, orElse: suspend (AIError) -> A): A =
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
  mockClient: MockOpenAIClient,
  block: suspend AIScope.() -> A,
  orElse: suspend (AIError) -> A
): A =
  try {
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
suspend fun <A> AI<A>.mock(mockAI: MockOpenAIClient): Either<AIError, A> =
  MockAIScope(mockAI, { invoke().right() }, { it.left() })

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. This method uses the [mockAI] to
 * compute the different responses.
 */
suspend fun <A> AI<A>.mock(mockAI: (String) -> String): Either<AIError, A> =
  MockAIScope(simpleMockAIClient(mockAI), { invoke().right() }, { it.left() })

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
