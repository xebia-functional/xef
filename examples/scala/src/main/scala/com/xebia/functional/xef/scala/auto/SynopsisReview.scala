package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.prompt.*
import io.circe.Decoder

final case class Play(title: String, era: String) derives PromptTemplate, Decoder, SerialDescriptor

final case class Synopsis(summary: String) derives PromptTemplate, Decoder, SerialDescriptor

final case class Review(review: String) derives PromptTemplate, Decoder, SerialDescriptor

final case class Score(score: Double) derives PromptTemplate, Decoder, SerialDescriptor

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

  def scoreTemplate(review: Review): String =
    s"""
       |You are an independent play critic scoring the best plays of the year.
       |Given the review of the play, it is your job to score this play.
       |
       |Play Review: ${review.review}.
       |Score the above play from 0 to 10:
    """.stripMargin

  val playScore = PromptTemplate[Play](Play("The power of Zuluastral", "Modern Era"))
    .chain[Synopsis](play => synopsisTemplate(play))
    .chain[Review](synopsis => reviewTemplate(synopsis))
    .chain[Score](review => scoreTemplate(review))
  println(s"Score (0 to 10): " + playScore.score)
}
