package com.xebia.functional.xef.auto

import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.AIClient

data class AIRuntime<A>(
  val client: AIClient,
  val embeddings: Embeddings,
  val runtime: suspend (block: AI<A>) -> A
) {
  companion object
}
