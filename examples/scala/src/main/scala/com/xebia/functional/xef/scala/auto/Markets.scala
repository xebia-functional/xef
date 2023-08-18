package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

import java.text.SimpleDateFormat
import java.util.Date

private final case class MarketNews(news: String, raisingStockSymbols: List[String], decreasingStockSymbols: List[String])
    derives SerialDescriptor,
      Decoder

@main def runMarkets: Unit =
  conversation {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, summon[ScalaConversation], 3)
    addContext(search.search(s"$currentDate Stock market results, raising stocks, decreasing stocks").get())
    val news = prompt[MarketNews](Prompt("Write a short summary of the stock market results given the provided context."))
    println(news)
  }
