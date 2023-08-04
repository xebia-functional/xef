package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.reasoning.wikipedia.WikipediaClient

suspend fun main() {
    ai {
        val client = WikipediaClient()

        val searchDataByPageId = WikipediaClient.SearchDataByParam(
            pageId = 5222
        )
        val answerByPageId = client.searchByPageId(searchDataByPageId)

        val searchDataByTitle = WikipediaClient.SearchDataByParam(
            title = "Departments of Colombia"
        )
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
    }.getOrThrow()
}
