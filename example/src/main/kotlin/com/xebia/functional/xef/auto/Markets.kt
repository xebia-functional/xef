package com.xebia.functional.xef.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import com.xebia.functional.agents.search
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class MarketNews(
  val news: String,
  val raisingStockSymbols: List<String>,
  val decreasingStockSymbols: List<String>
)

suspend fun main() {
  ai {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    context(search("$currentDate Stock market results, raising stocks, decreasing stocks")) {
      val news: MarketNews = prompt(
        """|
                     |Write a short summary of the stock market results given the provided context.
                     """.trimMargin()
      )
      println(news)
    }
  }.getOrElse { println(it) }
}

