package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

import scala.io.StdIn.readLine

private final case class AIResponse(answer: String) derives SerialDescriptor, Decoder

val pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

@main def runPDFDocument: Unit =
  ai {
    contextScope(pdf(resource = pdfUrl)) {
      while (true) {
        println("Enter your question: ")
        val line = scala.io.StdIn.readLine()
        val response = prompt[AIResponse](line)
        println(s"${response.answer}\n---\n")
      }
    }
  }.getOrElse(ex => println(ex.getMessage))
