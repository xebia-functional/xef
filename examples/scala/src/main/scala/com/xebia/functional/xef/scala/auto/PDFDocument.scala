package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode

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
  }.getOrElse(ex => println(ex.getMessage))
