package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import com.xebia.functional.tool.search
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.toDuration

@Serializable
data class MarketNewsItem(val date: String, val content: String)

@Serializable
data class MarketNews(
    val todaysDate: String,
    val news: List<MarketNewsItem>,
    val raisingStockSymbols: List<String>,
    val decreasingStockSymbols: List<String>
)

suspend fun main() {
    ai {
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        agent(
            *search(
                "$currentDate Stock market results"
            )
        ) {
            val news: MarketNews = ai(
                """|
                   |Write a short summary of the stock market results given the provided context
                   """.trimMargin()
            )
            println(news)
        }
    }.getOrElse { println(it) }
}

