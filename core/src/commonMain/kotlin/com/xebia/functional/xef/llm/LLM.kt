package com.xebia.functional.xef.llm

sealed interface LLM : AutoCloseable {
  val name: String

  override fun close() {}
}
