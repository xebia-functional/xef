package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture

actual class Search
@JvmOverloads
actual constructor(
  override val model: Chat,
  override val scope: Conversation,
  override val maxResultsInContext: Int,
) : SearchTool, AutoCloseable {

  private val coroutineScope = CoroutineScope(SupervisorJob())

  override val client = SerpApiClient()

  fun search(query: String): CompletableFuture<Array<out String>> {
    return coroutineScope.async { arrayOf(invoke(query)) }.asCompletableFuture()
  }

  override fun close() {
    client.close()
  }
}
