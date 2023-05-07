package com.xebia.functional.loaders

import com.xebia.functional.Document
import com.xebia.functional.textsplitters.BaseTextSplitter
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape

/** Creates a TextLoader based on a Path */
suspend fun ScrapeURLTextLoader(
    url: String
): BaseLoader = object : BaseLoader {

    override suspend fun load(): List<Document> =
        buildList {
            skrape(BrowserFetcher) {
                request {
                    this.url = url
                }
                response {
                    htmlDocument {
                        val cleanedText = cleanUpText(wholeText)
                        add(
                            Document("""|
                            |Title: $titleText
                            |Info: $cleanedText
                            """.trimIndent()
                            )
                        )

                    }
                }
            }
        }

    override suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<Document> =
        textSplitter.splitDocuments(documents = load())

    private tailrec fun cleanUpTextHelper(lines: List<String>, result: List<String> = listOf()): List<String> {
        return if (lines.isEmpty()) {
            result
        } else {
            val trimmedLine = lines.first().trim()
            val newResult = if (trimmedLine.isNotEmpty()) result + trimmedLine else result
            cleanUpTextHelper(lines.drop(1), newResult)
        }
    }

    private fun cleanUpText(text: String): String {
        val lines = text.split("\n")
        val cleanedLines = cleanUpTextHelper(lines)
        return cleanedLines.joinToString("\n")
    }
}
