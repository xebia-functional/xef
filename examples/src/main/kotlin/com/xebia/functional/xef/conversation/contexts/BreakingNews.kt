package com.xebia.functional.xef.conversation.contexts

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.Serializable

@Serializable data class BreakingNewsAboutCovid(val summary: String)

suspend fun main() {
  Conversation {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k)
    val search = Search(model = model, scope = this)
    val docs = search("$currentDate Covid News")
    addContext(docs)
    val news: BreakingNewsAboutCovid =
      prompt(
        Prompt(model) {
          +user("write a paragraph of about 300 words about: $currentDate Covid News")
        }
      )
    println(news)
  }
}
