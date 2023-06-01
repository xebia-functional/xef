package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.prompt.*
import io.circe.Decoder

final case class Play(title: String, era: String)

final case class Synopsis(summary: String) derives PromptTemplate, Decoder, ScalaSerialDescriptor

final case class Review(review: String) derives PromptTemplate, Decoder, ScalaSerialDescriptor

@main def runSynopsisReview: Unit = {
  def synopsisTemplate(play: Play): String =
    s"""
       |You are a playwright. Given the title of play and the era it is set in, it is your job to write a synopsis for that title.
       |
       |Title: ${play.title}.
       |Era: ${play.era}.
       |Playwright: This is a synopsis for the above play:
      """.stripMargin

  def reviewTemplate(synopsis: Synopsis): String =
    s"""
       |You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play.
       |
       |Play Synopsis: ${synopsis.summary}.
       |Review from a New York Times play critic of the above play:
    """.stripMargin

  val synopsisReview = PromptTemplate[Synopsis]
    .create(synopsisTemplate(Play("The power of Zuluastral", "Modern Era")))
    .add[Review](synopsis => reviewTemplate(synopsis))
  println(synopsisReview.review)
}
