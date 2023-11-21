package com.xebia.functional.xef.conversation.contexts

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search

suspend fun main() {

  val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_0613)
  val question =
    Prompt(model) { +user("Knowing this forecast, what clothes do you recommend I should wear?") }

  Conversation {
    val search = Search(model = model, scope = this)
    addContext(search("Weather in Cádiz, Spain"))
    val answer = promptMessage(question)
    println(answer)
  }
}
