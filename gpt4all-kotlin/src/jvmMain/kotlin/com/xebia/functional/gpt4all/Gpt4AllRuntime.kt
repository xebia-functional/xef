package com.xebia.functional.gpt4all

import arrow.core.Either
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AI
import com.xebia.functional.xef.auto.CoreAIScope

/**
 * Run the [AI] value to produce an [A], this method initialises all the dependencies required to
 * run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 */
suspend inline fun <A> AI<A>.getOrElse(crossinline orElse: suspend (AIError) -> A): A =
  AIScope(this) { orElse(it) }

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
 * Run the [AI] value to produce _either_ an [AIError], or [A]. this method initialises all the
 * dependencies required to run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @see getOrElse for an operator that allow directly handling the [AIError] case.
 */
suspend inline fun <reified A> AI<A>.toEither(): Either<AIError, A> =
  Either.catchOrThrow { getOrThrow() }

suspend fun <A> AIScope(block: AI<A>, orElse: suspend (AIError) -> A): A =
  try {
    val scope = CoreAIScope(HuggingFaceLocalEmbeddings.DEFAULT)
    block(scope)
  } catch (e: AIError) {
    orElse(e)
  }

