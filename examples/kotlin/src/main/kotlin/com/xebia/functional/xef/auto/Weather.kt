package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import io.github.oshai.kotlinlogging.KotlinLogging

suspend fun main() {
  val logger = KotlinLogging.logger("Weather")

  val question = Prompt("Knowing this forecast, what clothes do you recommend I should wear?")
  val answer = getQuestionAnswer(question)

  logger.info { answer }
}

private suspend fun getQuestionAnswer(question: Prompt): String =
  OpenAI.conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("Weather in Cádiz, Spain"))
    promptMessage(question)
  }
