package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

private final case class Weather(forecast: List[String]) derives ScalaSerialDescriptor, Decoder

@main def runWeather: Unit =
  def getQuestionAnswer(question: String): Weather = {
    ai {
      prompt[Weather](question)
    }
  }
  val question = "Knowing this forecast, what clothes do you recommend I should wear?"
  val answer: Weather = getQuestionAnswer(question)
  println(answer.forecast.mkString(","))
