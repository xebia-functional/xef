package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

@main def runWeather: Unit =
  def getQuestionAnswer(question: String): List[String] = {
    ai {
      prompt[List[String]](question)
    }
  }
  val question = "Knowing this forecast, what clothes do you recommend I should wear?"
  val answer: List[String] = getQuestionAnswer(question)
  println(answer.mkString(","))
