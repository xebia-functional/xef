package com.xebia.functional.xef.examples.scala.context.serpapi

import com.xebia.functional.xef.reasoning.pdf.PDF
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

import scala.io.StdIn.readLine

case class AIResponse(answer: String) derives SerialDescriptor, Decoder

val PdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

@main def runUserQueries(): Unit = conversation:
  val pdf = PDF(openAI.DEFAULT_CHAT, openAI.DEFAULT_SERIALIZATION, summon[ScalaConversation])
  addContext(Array(pdf.readPDFFromUrl.readPDFFromUrl(PdfUrl).get))
  while (true)
    println("Enter your question: ")
    val AIResponse(answer) = prompt[AIResponse](readLine())
    println(s"$answer\n---\n")
