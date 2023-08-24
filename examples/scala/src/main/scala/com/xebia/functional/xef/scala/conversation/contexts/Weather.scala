package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.prompt.Prompt

private def getQuestionAnswer(question: Prompt)(using conversation: ScalaConversation): String =
  val search: Search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, conversation, 3)
  addContext(search.search("Weather in Cádiz, Spain").get())
  promptMessage(question)

@main def runWeather: Unit =
  val question = Prompt("Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?")
  println(conversation(getQuestionAnswer(question)).mkString("\n"))
