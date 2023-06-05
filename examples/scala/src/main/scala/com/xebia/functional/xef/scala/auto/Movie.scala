package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Movie(title: String, genre: String, director: String) derives SerialDescriptor, Decoder

@main def runMovie: Unit =
  ai {
    val movie = prompt[Movie]("Inception movie genre and director.")
    println(s"The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
  }.getOrElse(ex => println(ex.getMessage))
