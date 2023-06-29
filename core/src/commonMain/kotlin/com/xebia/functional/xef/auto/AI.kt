package com.xebia.functional.xef.auto

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.AIClient
import com.xebia.functional.xef.vectorstores.VectorStore

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

suspend fun <A> AIScope(runtime: AIRuntime<A>, block: AI<A>, orElse: suspend (AIError) -> A): A =
  try {
    runtime.runtime(block)
  } catch (e: AIError) {
    orElse(e)
  }
