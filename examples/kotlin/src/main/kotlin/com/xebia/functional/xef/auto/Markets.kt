package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.serialization.Serializable

@Serializable
data class MarketNews(
  val news: String,
  val raisingStockSymbols: List<String>,
  val decreasingStockSymbols: List<String>
)

suspend fun main() {
  OpenAI.conversation {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("$currentDate Stock market results, raising stocks, decreasing stocks"))
    val news: MarketNews =
      prompt(
        """|
                     |Write a short summary of the stock market results given the provided context.
                     """
          .trimMargin()
      )
    println(news)
  }
}
