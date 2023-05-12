package com.xebia.functional.agents

import arrow.core.flatten
import arrow.fx.coroutines.parMap
import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.xebia.functional.textsplitters.BaseTextSplitter
import io.ktor.http.*
import java.util.stream.Collectors
import kotlin.jvm.optionals.toList
import kotlinx.coroutines.Dispatchers

fun bingSearch(
  search: String,
  splitter: BaseTextSplitter,
  url: String = "https://www.bing.com/news/search?q=${search.encodeURLParameter()}&format=rss",
  maxLinks: Int = 10
): ParameterlessAgent<List<String>> =
  ParameterlessAgent<List<String>>(
    name = "Bing Search",
    description = "Searches Bing for $search",
  ) {
    val items: List<Item> = RssReader().read(url).collect(Collectors.toList())
    val links = items.map { it.link }.flatMap { it.toList() }.take(maxLinks)
    val linkedDocs =
      links
        .parMap(Dispatchers.IO) { link ->
          try {
            with(scrapeUrlContent(link, splitter)) { call() }
          } catch (e: Exception) {
            // ignore errors when scrapping nested content due to certificates and other remote
            // issues
            emptyList<String>()
          }
        }
        .flatten()
    val docs =
      items.map {
        """|
                    |${it.title}
                    |${it.description}
                    |${it.link}
                    |${it.pubDate}
                """
          .trimMargin()
      }
    splitter.splitDocuments(linkedDocs + docs)
  }
