package com.xebia.functional.xef.conversation.contexts

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.Serializable

@Serializable data class BreakingNewsAboutCovid(val summary: String)

suspend fun main() {
  OpenAI.conversation {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    val search =
      Search(chatApi = OpenAI.fromEnvironment().DEFAULT_CHAT, scope = this, maxResultsInContext = 3)
    val docs = search("$currentDate Covid News")
    addContext(docs)
    val news: BreakingNewsAboutCovid =
      prompt("write a paragraph of about 300 words about: $currentDate Covid News")
    println(news)
  }
}
