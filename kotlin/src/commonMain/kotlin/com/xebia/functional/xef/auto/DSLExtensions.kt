package com.xebia.functional.xef.auto

/** A DSL block that makes it more convenient to construct [AI] values. */
inline fun <A> conversation(noinline block: suspend Conversation.() -> A): AI<A> = block
