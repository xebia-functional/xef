package com.xebia.functional.loaders

import com.xebia.functional.Document
import com.xebia.functional.textsplitters.BaseTextSplitter
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.p
import it.skrape.selects.text

/** Creates a TextLoader based on a Path */
suspend fun ScrapeURLTextLoader(
    url: String
): BaseLoader = object : BaseLoader {

    override suspend fun load(): List<Document> = buildList {
        skrape(BrowserFetcher) {
            request {
                this.url = url
            }
            response {
                htmlDocument {
//          val titles: String = h1 {
//            findFirst {
//              text
//            }
//          }
//          val info: String = p {
//            findAll {
//              text
//            }
//          }
//          val links: String = a {
//            findAll {
//              text
//            }
//          }
//
                    add(
                        Document(
                            """|
                            |Title: $titleText
                            |Info: $wholeText
                            """.trimIndent()
                        )
                    )
                }
            }
        }
    }

    override suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<Document> =
        textSplitter.splitDocuments(documents = load())
}
