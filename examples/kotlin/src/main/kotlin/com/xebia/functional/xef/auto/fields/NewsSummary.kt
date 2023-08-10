package com.xebia.functional.xef.auto.fields

import com.xebia.functional.xef.agents.search
import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class NewsSummary(
    @Description(["A title for the relevant news article."])
    val title: String,
    @Description(["A description of the relevant news article"])
    val author: String,
    @Description(["A 50 word summary of the article."])
    val summary: String
)

@Serializable
data class NewsItems(
    @Description(["A list of news items about the context"])
    val items: List<NewsSummary>
)


suspend fun main() {
    conversation {
        contextScope(search("Covid news on ${LocalDate.now()}")) {
            val news: NewsItems = prompt()
            println(news)
        }

    }.getOrElse { println(it) }
}
