package com.xebia.functional.xef.reasoning.serpapi

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.promptMessages
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.Tool
import kotlin.jvm.JvmSynthetic

interface SearchTool : Tool {
  val model: OpenAIModel<CreateChatCompletionRequestModel>
  val chatApi: ChatApi
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
    return chatApi
      .promptMessages(
        prompt =
          Prompt(model) {
            +system("Search results:")
            docs.searchResults.take(maxResultsInContext).forEach {
              +system("Title: ${it.title}")
              +system("Source: ${it.source}")
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
