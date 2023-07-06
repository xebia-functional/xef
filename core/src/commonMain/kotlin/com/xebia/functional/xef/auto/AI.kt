package com.xebia.functional.xef.auto

import com.xebia.functional.xef.vectorstores.VectorStore

/**
 * An [AI] value represents an action relying on artificial intelligence that can be run to produce
 * an `A`. This value is _lazy_ and can be combined with other `AI` values using
 * [CoreAIScope.invoke], and thus forms a monadic DSL.
 *
 * All [AI] actions that are composed together using [CoreAIScope.invoke] share the same
 * [VectorStore], [OpenAIEmbeddings] and [AIClient] instances.
 */
typealias AI<A> = suspend CoreAIScope.() -> A
