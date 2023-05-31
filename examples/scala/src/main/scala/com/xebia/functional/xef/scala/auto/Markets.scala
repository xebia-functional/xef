package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.agents.DefaultSearch
import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

import java.text.SimpleDateFormat
import java.util.Date

private final case class MarketNews(news: String, raisingStockSymbols: List[String], decreasingStockSymbols: List[String])
    derives ScalaSerialDescriptor,
      Decoder

@main def runMarkets: Unit =
  ai {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    contextScope(DefaultSearch.search(s"$currentDate Stock market results, raising stocks, decreasing stocks")) {
      val news = prompt[MarketNews]("Write a short summary of the stock market results given the provided context.")
      println(news)
    }
  }.getOrElse(ex => println(ex.getMessage))
