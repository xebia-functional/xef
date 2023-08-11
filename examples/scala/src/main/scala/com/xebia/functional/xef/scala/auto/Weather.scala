package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.agents.DefaultSearch
import com.xebia.functional.xef.scala.auto.*

private def getQuestionAnswer(question: String): String = conversation {
  addContext(DefaultSearch.search("Weather in Cádiz, Spain"))
  promptMessage(question)
}

@main def runWeather: Unit =
  val question = "Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?"
  println(getQuestionAnswer(question).mkString("\n"))
