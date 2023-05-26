package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

import scala.io.StdIn.readLine

private final case class AIResponse(answer: String) derives ScalaSerialDescriptor, Decoder
@main def runPDFDocument: Unit =
  ai {
    while (true) {
      println("Enter your question: ")
      val line = scala.io.StdIn.readLine()
      val response = prompt[AIResponse](line)
      println(s"${response.answer}\n---\n")
    }
  }
