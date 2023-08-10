package com.xebia.functional.xef.reasoning.search

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.tools.ToolWikipedia
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient.*

class SearchWikipedia
@JvmOverloads
constructor(
  private val model: Chat,
  private val scope: Conversation,
  private val maxResultsInContext: Int = 3,
  private val client: WikipediaClient = WikipediaClient()
) : ToolWikipedia, AutoCloseable, AutoClose by autoClose() {
  override val name: String = "SearchWikipedia"

  override val description: String =
    "Search in Wikipedia for information. The tool input is a simple one line string or the number of the page id or the title"

  override suspend fun invoke(input: String?, pageId: Int?, title: String?): String {
    return if (!input.isNullOrBlank()) {
      val docs = client.search(SearchData(input))
      model
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
    } else if (!title.isNullOrBlank()) {
      searchByParams(title = title)
    } else {
      searchByParams(pageId = pageId)
    }
  }

  private suspend fun searchByParams(pageId: Int? = null, title: String? = null): String {
    val docs =
      if (!title.isNullOrBlank()) client.searchByTitle(SearchDataByTitle(title))
      else client.searchByPageId(SearchDataByPageId(pageId))

    return model
      .promptMessages(
        messages =
          listOf(Message.systemMessage { "Search results:" }) +
            listOf(
              Message.systemMessage { "Title: ${docs.title}" },
              Message.systemMessage { "PageId: ${docs.pageId}" },
              Message.systemMessage { "Content: ${docs.document}" }
            ) +
            listOf(
              Message.userMessage { "input: ${title ?: pageId}" },
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
