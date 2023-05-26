package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode

import java.text.SimpleDateFormat
import java.util.Date

private final case class BreakingNewsAboutCovid(summary: String) derives ScalaSerialDescriptor, Decoder

@main def runBreakingNews: Unit =
  val sdf = SimpleDateFormat("dd/M/yyyy")
  val currentDate = sdf.format(Date())
  val news = ai(prompt[BreakingNewsAboutCovid](s"Write a paragraph of about 300 words about: $currentDate Covid News"))
  println(news)
