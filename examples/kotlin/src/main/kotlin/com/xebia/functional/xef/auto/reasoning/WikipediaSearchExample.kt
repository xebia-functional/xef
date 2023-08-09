package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient

suspend fun main() {
    ai {
        val client = WikipediaClient()

        val searchData = WikipediaClient.SearchData(
            search = "Capital%20de%20Colombia"
        )

        val answer = client.search(searchData)

        answer.searchResults.searches.forEach {
            println(
                "\n\uD83E\uDD16 Search Information:\n\n" +
                        "Title: ${it.title}\n" +
                        "PageId: ${it.pageId}\n" +
                        "Size: ${it.size}\n" +
                        "WordCount: ${it.wordCount}\n" +
                        "Document: ${it.document}\n"
            )
        }
    }.getOrThrow()
}
