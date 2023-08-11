package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.search
import com.xebia.functional.xef.auto.llm.openai.conversation
import com.xebia.functional.xef.auto.llm.openai.prompt
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.serialization.Serializable

@Serializable data class BreakingNewsAboutCovid(val summary: String)

suspend fun main() {
  conversation {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    val docs = search("$currentDate Covid News")
    addContext(docs)
    val news: BreakingNewsAboutCovid =
      prompt("write a paragraph of about 300 words about: $currentDate Covid News")
    println(news)
  }
}
