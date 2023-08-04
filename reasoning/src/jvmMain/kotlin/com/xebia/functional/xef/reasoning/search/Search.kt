package com.xebia.functional.xef.reasoning.search

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.reasoning.serpapi.SerpApiClient
import com.xebia.functional.xef.reasoning.tools.Tool

class Search
@JvmOverloads
constructor(
  private val model: Chat,
  private val scope: CoreAIScope,
  private val client: SerpApiClient = SerpApiClient()
) : Tool, AutoCloseable, AutoClose by autoClose() {
  override val name: String = "Search"

  override val description: String =
    "Search the web for the best answer. The tool input is a simple string"

  override suspend fun invoke(input: String): String {
    val docs = client.search(SerpApiClient.SearchData(input))
    val innerDocs = docs.searchResults.mapNotNull { it.document }
    scope.extendContext(*innerDocs.toTypedArray())
    return model.promptMessage(
      question =
        """|
        |Given the following input:
        |```input
        |${input}
        |```
        |Provide information that helps with the `input`. 
      """
          .trimMargin(),
      context = scope.context,
    )
  }

  override fun close() {
    client.close()
  }
}
