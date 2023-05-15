package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.search
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date

@Serializable
data class BreakingNewsAboutCovid(
  val summary: String
)

suspend fun main() {
  ai {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    context(search("$currentDate Covid News")) {
      val news: BreakingNewsAboutCovid =
        prompt("write a paragraph of about 300 words about: $currentDate Covid News")
      println(news)
    }
  }.getOrElse { println(it) }
}
