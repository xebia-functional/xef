@file:JvmName("OpenAIRuntime")

package com.xebia.functional.xef.auto.llm.openai

import arrow.core.Either
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AI
import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlin.jvm.JvmName

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
suspend inline fun <A> AI<A>.getOrThrow(): A = getOrElse { throw it }

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. this method initialises all the
 * dependencies required to run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @see getOrElse for an operator that allow directly handling the [AIError] case.
 */
suspend inline fun <A> AI<A>.toEither(): Either<AIError, A> = Either.catchOrThrow { getOrThrow() }

suspend fun <A> AIScope(block: AI<A>, orElse: suspend (AIError) -> A): A =
  try {
    OpenAIScope().use { block(it) }
  } catch (e: AIError) {
    orElse(e)
  }

private class OpenAIScope: CoreAIScope, AutoClose by autoClose() {
  override val embeddings: Embeddings = OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING)
  override val context: VectorStore = com.xebia.functional.xef.vectorstores.LocalVectorStore(embeddings)
  override val conversationId: ConversationId = ConversationId(UUID.generateUUID().toString())
}
