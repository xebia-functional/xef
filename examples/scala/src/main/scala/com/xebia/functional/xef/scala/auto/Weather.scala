package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.agents.DefaultSearch
import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

private def getQuestionAnswer(question: String): List[String] = ai {
  contextScope(DefaultSearch.search("Weather in Cádiz, Spain")) {
    promptMessage(question)
  }
}.getOrElse(ex => throw IllegalStateException(ex.getMessage))
@main def runWeather: Unit =
  val question = "Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?"
  println(getQuestionAnswer(question).mkString("\n"))
