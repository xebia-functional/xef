package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient.SearchDataByTitle
import kotlin.jvm.JvmOverloads

class SearchWikipediaByTitle
@JvmOverloads
constructor(
  private val model: Chat,
  private val scope: Conversation,
  private val client: WikipediaClient = WikipediaClient()
) : Tool, AutoCloseable, AutoClose by autoClose() {
  override val name: String = "SearchWikipediaByTitle"

  override val description: String =
    "Search in Wikipedia for detail information. The tool input is the title of the page"

  override suspend fun invoke(input: String): String {
    val docs = client.searchByTitle(SearchDataByTitle(input))

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
