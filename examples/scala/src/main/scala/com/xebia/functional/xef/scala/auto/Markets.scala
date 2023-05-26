package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

import java.text.SimpleDateFormat
import java.util.Date

private final case class MarketNews(news: String, raisingStockSymbols: List[String], decreasingStockSymbols: List[String])
    derives ScalaSerialDescriptor,
      Decoder

@main def runMarkets: Unit =
  val sdf = SimpleDateFormat("dd/M/yyyy")
  val currentDate = sdf.format(Date())
  val news = ai(prompt[MarketNews]("""|Write a short summary of the stock market results given the provided context.
    """.stripMargin))
  println(news)
