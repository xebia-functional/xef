package com.xebia.functional.tool

import arrow.core.flatten
import arrow.fx.coroutines.parMap
import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.xebia.functional.Document
import com.xebia.functional.auto.logger
import com.xebia.functional.textsplitters.BaseTextSplitter
import com.xebia.functional.tools.Tool
import io.ktor.http.*
import java.util.stream.Collectors
import kotlin.jvm.optionals.toList

fun bingSearch(
    search: String,
    splitter: BaseTextSplitter,
    url: String = "https://www.bing.com/news/search?q=${search.encodeURLParameter()}&format=rss",
): Tool =
    Tool(
        name = "Bing Search",
        description = "Searches Bing for $search",
    ) {
        val items: List<Item> = RssReader().read(url)
            .collect(Collectors.toList())
        val links = items.map { it.link }.flatMap { it.toList() }
        val linkedDocs = links.parMap { link ->
            try {
                scrapeUrlContent(link, splitter).action(link)
            } catch (e: Exception) {
                //ignore errors when scrapping nested content due to certificates and other remote issues
                logger.debug { "Error scrapping $it" }
                emptyList()
            }
        }.flatten()
        val docs = items.map {
            Document(
                """|
                    |${it.title}
                    |${it.description}
                    |${it.link}
                    |${it.pubDate}
                """.trimMargin()
            )
        }
        splitter.splitDocuments(linkedDocs + docs)
    }
