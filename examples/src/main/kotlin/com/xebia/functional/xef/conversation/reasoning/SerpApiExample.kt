package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.reasoning.serpapi.SerpApiClient

suspend fun main() {
  val client = SerpApiClient()

  val searchData =
    SerpApiClient.SearchData(
      search = "german+shepperd",
    )

  val answer = client.search(searchData)

  answer.searchResults?.forEach {
    println(
      "\n\uD83E\uDD16 Search Information:\n\n" +
        "Title: ${it.title}\n" +
        "Document: ${it.document}\n" +
        "Source: ${it.source}\n"
    )
  } ?: println("No results found")
}
