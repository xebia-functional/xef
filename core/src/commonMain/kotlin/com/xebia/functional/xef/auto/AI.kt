package com.xebia.functional.xef.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.AIClient
import com.xebia.functional.xef.llm.LLMModel
import com.xebia.functional.xef.llm.openai.MockOpenAIClient
import com.xebia.functional.xef.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.llm.openai.simpleMockAIClient
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlin.time.ExperimentalTime

@DslMarker annotation class AiDsl

/**
 * An [AI] value represents an action relying on artificial intelligence that can be run to produce
 * an `A`. This value is _lazy_ and can be combined with other `AI` values using
 * [CoreAIScope.invoke], and thus forms a monadic DSL.
 *
 * All [AI] actions that are composed together using [CoreAIScope.invoke] share the same
 * [VectorStore], [OpenAIEmbeddings] and [AIClient] instances.
 */
typealias AI<A> = suspend CoreAIScope.() -> A

/** A DSL block that makes it more convenient to construct [AI] values. */
inline fun <A> ai(noinline block: suspend CoreAIScope.() -> A): AI<A> = block

/**
 * Run the [AI] value to produce an [A], this method initialises all the dependencies required to
 * run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 */
suspend inline fun <A> AI<A>.getOrElse(
  runtime: AIRuntime<A> = AIRuntime.openAI(),
  crossinline orElse: suspend (AIError) -> A
): A = AIScope(runtime, this) { orElse(it) }

suspend fun <A> AIScope(runtime: AIRuntime<A>, block: AI<A>, orElse: suspend (AIError) -> A): A =
  try {
    runtime.runtime(block)
  } catch (e: AIError) {
    orElse(e)
  }

@OptIn(ExperimentalTime::class)
suspend fun <A> MockAIScope(
  mockClient: MockOpenAIClient,
  block: suspend CoreAIScope.() -> A,
  orElse: suspend (AIError) -> A
): A =
  try {
    val embeddings = OpenAIEmbeddings(mockClient)
    val vectorStore = LocalVectorStore(embeddings)
    val scope =
      CoreAIScope(
        LLMModel.GPT_3_5_TURBO,
        LLMModel.GPT_3_5_TURBO_FUNCTIONS,
        mockClient,
        vectorStore,
        embeddings
      )
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
