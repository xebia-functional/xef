package com.xebia.functional.xef.agents

import arrow.core.flatten
import arrow.fx.coroutines.parMap
import com.apptasticsoftware.rssreader.Item
import com.apptasticsoftware.rssreader.RssReader
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.textsplitters.TextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.github.oshai.KotlinLogging
import io.ktor.http.*
import java.util.stream.Collectors
import kotlin.jvm.optionals.toList
import kotlinx.coroutines.Dispatchers

suspend fun bingSearch(
  search: String,
  splitter: TextSplitter =
    TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50),
  url: String = "https://www.bing.com/news/search?q=${search.encodeURLParameter()}&format=rss",
  maxLinks: Int = 10
): List<String> {
  val logger = KotlinLogging.logger {}
  val items: List<Item> = RssReader().read(url).collect(Collectors.toList())
  val links = items.map { it.link }.flatMap { it.toList() }.take(maxLinks)
  val linkedDocs =
    links
      .parMap(Dispatchers.IO) { link ->
        logger.debug { "🔗: clicked on $link" }
        try {
          scrapeUrlContent(link, splitter)
        } catch (e: Exception) {
          // ignore errors when scrapping nested content due to certificates and other remote issues
          emptyList()
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
  return splitter.splitDocuments(linkedDocs + docs)
}
