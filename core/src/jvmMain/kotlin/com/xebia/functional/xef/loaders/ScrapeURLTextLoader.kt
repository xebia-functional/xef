package com.xebia.functional.xef.loaders

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape

/** Creates a TextLoader based on a Path */
suspend fun ScrapeURLTextLoader(url: String): BaseLoader =
  object : BaseLoader {

    override suspend fun load(): List<String> = buildList {
      skrape(BrowserFetcher) {
        request { this.url = url }
        response {
          htmlDocument {
            val cleanedText = cleanUpText(wholeText)
            add(
              """|
                            |Title: $titleText
                            |Info: $cleanedText
                            """
                .trimIndent()
            )
          }
        }
      }
    }

    private tailrec fun cleanUpTextHelper(
      lines: List<String>,
      result: List<String> = listOf()
    ): List<String> =
      if (lines.isEmpty()) result
      else {
        val trimmedLine = lines.first().trim()
        val newResult = if (trimmedLine.isNotEmpty()) result + trimmedLine else result
        cleanUpTextHelper(lines.drop(1), newResult)
      }

    private fun cleanUpText(text: String): String {
      val lines = text.split("\n")
      val cleanedLines = cleanUpTextHelper(lines)
      return cleanedLines.joinToString("\n")
    }
  }
