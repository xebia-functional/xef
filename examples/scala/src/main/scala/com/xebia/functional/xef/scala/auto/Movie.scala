package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

private final case class Movie(title: String, genre: String, director: String) derives SerialDescriptor, Decoder

@main def runMovie: Unit =
  conversation {
    val movie = prompt[Movie](Prompt("Inception movie genre and director."))
    println(s"The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
  }
