package com.xebia.functional.xef.auto

/** A DSL block that makes it more convenient to construct [AI] values. */
inline fun <A> ai(noinline block: suspend CoreAIScope.() -> A): AI<A> = block
