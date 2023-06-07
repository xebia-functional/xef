package com.xebia.functional.xef.auto

import kotlinx.serialization.Serializable

@Serializable
data class Play(val title: String, val era: String)

@Serializable
data class Synopsis(val summary: String)

@Serializable
data class Review(val review: String)

@Serializable
data class Score(val score: Double)

suspend fun main() {
    ai {
        val playScore = Play("The power of Zuluastral", "Modern Era")
            .chain<Play, Synopsis>(this) { play -> synopsisTemplate(play) }
            .chain<Synopsis, Review>(this) { synopsis -> reviewTemplate(synopsis) }
            .chain<Review, Score>(this) { review -> scoreTemplate(review) }
        println("Score (0 to 10): ${playScore.score}")
    }.getOrElse { println(it) }
}

private fun synopsisTemplate(play: Play): String =
       """
       |You are a playwright. Given the title of play and the era it is set in, it is your job to write a synopsis for that title.
       |
       |Title: ${play.title}.
       |Era: ${play.era}.
       |Playwright: This is a synopsis for the above play:
      """.trimMargin()

private fun reviewTemplate(synopsis: Synopsis): String =
    """
       |You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play.
       |
       |Play Synopsis: ${synopsis.summary}.
       |Review from a New York Times play critic of the above play:
    """.trimMargin()

private fun scoreTemplate(review: Review): String =
    """
       |You are an independent play critic scoring the best plays of the year.
       |Given the review of the play, it is your job to score this play.
       |
       |Play Review: ${review.review}.
       |Score the above play from 0 to 10:
    """.trimMargin()