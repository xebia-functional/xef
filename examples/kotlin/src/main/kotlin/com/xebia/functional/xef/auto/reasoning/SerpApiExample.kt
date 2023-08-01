package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.reasoning.serpapi.SerpApiClient

suspend fun main() {
    ai {
        val client = SerpApiClient()

        val searchData = SerpApiClient.SearchData("german+shepper", "Villavicencio,+Meta,+Colombia", "en", "us", "google.com")
        //val searchData = SerpApiClient.SearchData("Coffee", "Austin,+Texas,+United+States", "en", "us", "google.com", apiKey)

        val answer = client.search(searchData)

        answer.searchResults.forEach {
            println(
                "\n\uD83E\uDD16 Search Information:\n\n" +
                        "Title: ${it.title}\n" +
                        "Document: ${it.document}\n" +
                        "Source: ${it.source}\n"
            )
        }
    }.getOrThrow()
}
