package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.tools.Tool
import kotlin.jvm.JvmSynthetic

interface SearchTool : Tool {

  val model: Chat
  val scope: Conversation
  val maxResultsInContext: Int
  val client: SerpApiClient

  override val name: String
    get() = "Search"

  override val description: String
    get() = "Search the web for information. The tool input is a simple one line string"

  @JvmSynthetic
  override suspend fun invoke(input: String): String {

    val docs = client.search(SerpApiClient.SearchData(input))
    return model
      .promptMessages(
        messages =
          listOf(Message.systemMessage { "Search results:" }) +
            docs.searchResults.take(maxResultsInContext).flatMap {
              listOf(
                Message.systemMessage { "Title: ${it.title}" },
                Message.systemMessage { "Source: ${it.source}" },
                Message.systemMessage { "Content: ${it.document}" },
              )
            } +
            listOf(
              Message.userMessage { "input: $input" },
              Message.assistantMessage {
                "I will select the best search results and reply with information relevant to the `input`"
              }
            ),
        scope = scope,
      )
      .firstOrNull()
      ?: "No results found"
  }

  fun close() {
    client.close()
  }
}
