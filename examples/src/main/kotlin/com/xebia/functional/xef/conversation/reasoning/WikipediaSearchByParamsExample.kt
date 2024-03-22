package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient

suspend fun main() {
  val client = WikipediaClient()

  val searchDataByPageId = WikipediaClient.SearchDataByPageId(5222)
  val answerByPageId = client.searchByPageId(searchDataByPageId)

  val searchDataByTitle = WikipediaClient.SearchDataByTitle("Departments of Colombia")
  val answerByTitle = client.searchByTitle(searchDataByTitle)

  println(
    "\n\uD83E\uDD16 Search Information:\n\n" +
      "Title: ${answerByPageId.title}\n" +
      "PageId: ${answerByPageId.pageId}\n" +
      "Document: ${answerByPageId.document}\n"
  )

  println(
    "\n\uD83E\uDD16 Search Information:\n\n" +
      "Title: ${answerByTitle.title}\n" +
      "PageId: ${answerByTitle.pageId}\n" +
      "Document: ${answerByTitle.document}\n"
  )
}
