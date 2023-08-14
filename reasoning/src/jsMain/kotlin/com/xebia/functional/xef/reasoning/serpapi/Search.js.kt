package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat

actual class Search
actual constructor(
  override val model: Chat,
  override val scope: Conversation,
  override val maxResultsInContext: Int,
) : SearchTool, AutoCloseable {
  override val client: SerpApiClient = SerpApiClient()

  override fun close() {
    client.close()
  }
}
