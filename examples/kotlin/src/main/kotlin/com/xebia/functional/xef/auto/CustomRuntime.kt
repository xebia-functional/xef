package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.MockAIScope
import com.xebia.functional.xef.auto.llm.openai.simpleMockAIClient
import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig

suspend fun main() {
  val program = ai {
    val love: List<String> = promptMessage("tell me you like me with just emojis")
    println(love)
  }
  program.getOrElse(customRuntime()) { println(it) }
}

private fun fakeEmbeddings(): Embeddings = object : Embeddings {
  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<Embedding> = emptyList()

  override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
    emptyList()
}

private fun <A> customRuntime(): AIRuntime<A> {
  val client = simpleMockAIClient { it }
  return AIRuntime(client, fakeEmbeddings()) { block ->
    MockAIScope(
      client,
      block
    ) { throw it }
  }
}
