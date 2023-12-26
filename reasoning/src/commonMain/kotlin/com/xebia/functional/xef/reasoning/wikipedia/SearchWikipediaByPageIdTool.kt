package com.xebia.functional.xef.reasoning.wikipedia

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient.SearchDataByPageId

interface SearchWikipediaByPageIdTool : Tool {

  val chatApi: ChatApi
  val model: OpenAIModel<CreateChatCompletionRequestModel>
  val scope: Conversation
  val client: WikipediaClient

  override val name: String
    get() = "SearchWikipediaByPageId"

  override val description: String
    get() =
      "Search secondary tool in Wikipedia for detail information. The tool input is the number of page id, this tool can only be used with valid Wikipedia page ids returned by the primary search tool"

  override suspend fun invoke(input: String): String {
    val docs = client.searchByPageId(SearchDataByPageId(input.toInt()))

    return chatApi
      .promptMessages(
        prompt =
          Prompt(model) {
            +system("Search results:")
            +system("Title: ${docs.title}")
            +system("PageId: ${docs.pageId}")
            +system("Content: ${docs.document}")
            +user("input: $input")
            +assistant(
              "I will select the best search results and reply with information relevant to the `input`"
            )
          },
        scope = scope,
      )
      .firstOrNull() ?: "No results found"
  }

  fun close() {
    client.close()
  }
}
