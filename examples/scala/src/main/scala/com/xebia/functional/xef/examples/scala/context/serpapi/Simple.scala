package com.xebia.functional.xef.examples.scala.context.serpapi

import com.xebia.functional.xef.conversation.llm.openai.*
import com.xebia.functional.xef.reasoning.serpapi.*
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

import java.text.SimpleDateFormat
import java.util.Date

val openAI: OpenAI = OpenAI.fromEnvironment()

val sdf = SimpleDateFormat("dd/M/yyyy")
def currentDate: String = sdf.format(new Date)

def setContext(query: String)(using conversation: ScalaConversation): Unit =
  addContext(Search(openAI.DEFAULT_CHAT, conversation, 3).search(query).get)

case class BreakingNews(summary: String) derives SerialDescriptor, Decoder

case class MarketNews(news: String, risingStockSymbols: List[String], fallingStockSymbols: List[String]) derives SerialDescriptor, Decoder

case class Estimate(number: Long) derives SerialDescriptor, Decoder

@main def runWeather(): Unit = conversation:
  setContext("Weather in Cádiz, Spain")
  val question = "Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?"
  val answer = promptMessage(question)
  println(answer)

@main def runBreakingNews(): Unit = conversation:
  setContext(s"$currentDate COVID News")
  val BreakingNews(summary) = prompt[BreakingNews](s"Write a summary of about 300 words given the provided context.")
  println(summary)

@main def runMarketNews(): Unit = conversation:
  setContext(s"$currentDate Stock market results, rising stocks, falling stocks")
  val news = prompt[MarketNews]("Write a short summary of the stock market results given the provided context.")
  println(news)

@main def runFermiEstimate(): Unit = conversation:
  setContext("Estimate the number of medical needles in the world")
  val Estimate(needlesInWorld) = prompt[Estimate]("Answer the question with an integer number given the provided context.")
  println(s"Needles in world: $needlesInWorld")
