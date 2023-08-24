package com.xebia.functional.xef.conversation.contexts

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.serpapi.Search

suspend fun main() {

  val question = Prompt("Knowing this forecast, what clothes do you recommend I should wear?")

  OpenAI.conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("Weather in Cádiz, Spain"))
    val answer = promptMessage(question)
    println(answer)
  }
}
