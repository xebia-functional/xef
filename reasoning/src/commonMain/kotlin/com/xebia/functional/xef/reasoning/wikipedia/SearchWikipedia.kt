package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient.SearchData
import kotlin.jvm.JvmOverloads

class SearchWikipedia
@JvmOverloads
constructor(
  private val model: Chat,
  private val scope: Conversation,
  private val maxResultsInContext: Int = 3,
  private val client: WikipediaClient = WikipediaClient()
) : Tool, AutoCloseable, AutoClose by autoClose() {
  override val name: String = "SearchWikipedia"

  override val description: String =
    "Search in Wikipedia for information. The tool input is a simple one line string"

  override suspend fun invoke(input: String): String {
    val docs = client.search(SearchData(input))
    return model
      .promptMessages(
        messages =
          listOf(Message.systemMessage { "Search results:" }) +
            docs.searchResults.searches.take(maxResultsInContext).flatMap {
              listOf(
                Message.systemMessage { "Title: ${it.title}" },
                Message.systemMessage { "PageId: ${it.pageId}" },
                Message.systemMessage { "Size: ${it.size}" },
                Message.systemMessage { "WordCount: ${it.wordCount}" },
                Message.systemMessage { "Content: ${it.document}" }
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

  override fun close() {
    client.close()
  }
}
