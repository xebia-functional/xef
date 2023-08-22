package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient.SearchData

interface SearchWikipediaTool : Tool {

  val model: Chat
  val scope: Conversation
  val maxResultsInContext: Int
  val client: WikipediaClient

  override val name: String
    get() = "SearchWikipediaTool"

  override val description: String
    get() = "Search primary tool in Wikipedia for information. The tool input is a simple one line string"

  override suspend fun invoke(input: String): String {
    val docs = client.search(SearchData(input))

    return model
      .promptMessages(
        prompt =
          Prompt {
            +system("Search results:")
            docs.searchResults.searches.take(maxResultsInContext).forEach {
              +system("Title: ${it.title}")
              +system("PageId: ${it.pageId}")
              +system("Size: ${it.size}")
              +system("WordCount: ${it.wordCount}")
              +system("Content: ${it.document}")
            }
            +user("input: $input")
            +assistant(
              "I will select the best search results and reply with information relevant to the `input`"
            )
          },
        scope = scope,
      )
      .firstOrNull()
      ?: "No results found"
  }

  fun close() {
    client.close()
  }
}
