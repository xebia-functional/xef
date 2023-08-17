package com.xebia.functional.xef.auto.fields

import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class NewsSummary(
  @Description(["A title for the relevant news article."]) val title: String,
  @Description(["A description of the relevant news article"]) val author: String,
  @Description(["A 50 word summary of the article."]) val summary: String
)

@Serializable
data class NewsItems(
  @Description(["A list of news items about the context"]) val items: List<NewsSummary>
)

suspend fun main() {
  OpenAI.conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("Covid news on ${LocalDate.now()}"))
    val news: NewsItems = prompt(Prompt("Provide news about covid."))
    println(news)
  }
}
