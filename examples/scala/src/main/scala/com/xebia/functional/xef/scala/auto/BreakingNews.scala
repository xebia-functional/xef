package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.agents.DefaultSearch
import io.circe.Decoder

import java.text.SimpleDateFormat
import java.util.Date

private final case class BreakingNewsAboutCovid(summary: String) derives SerialDescriptor, Decoder

@main def runBreakingNews: Unit =
  ai {
    val sdf = SimpleDateFormat("dd/M/yyyy")
    val currentDate = sdf.format(Date())
    contextScope(DefaultSearch.search(s"$currentDate Covid News")) {
      val news = prompt[BreakingNewsAboutCovid](s"Write a paragraph of about 300 words about: $currentDate Covid News")
      println(news.summary)
    }
  }.getOrElse(ex => println(ex.getMessage))
