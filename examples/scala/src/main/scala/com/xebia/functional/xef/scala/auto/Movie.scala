package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Movie(title: String, genre: String, director: String) derives ScalaSerialDescriptor, Decoder

@main def runMovie: Unit =
  val movie = ai(prompt[Movie]("Inception movie genre and director."))
  println(s"The movie ${movie.title} is a ${movie.genre} film directed by ${movie.director}.")
