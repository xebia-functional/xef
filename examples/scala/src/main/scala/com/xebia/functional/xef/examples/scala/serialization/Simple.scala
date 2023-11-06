package com.xebia.functional.xef.examples.scala.serialization

import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

case class AsciiArt(art: String) derives SerialDescriptor, Decoder

case class Book(title: String, author: String, pages: Int) derives SerialDescriptor, Decoder

case class City(population: Int, description: String) derives SerialDescriptor, Decoder

case class Movie(title: String, genre: String, director: String) derives SerialDescriptor, Decoder

case class Recipe(name: String, ingredients: List[String]) derives SerialDescriptor, Decoder

@main def runAsciiArt(): Unit = conversation:
  val AsciiArt(art) = prompt[AsciiArt]("ASCII art of a cat dancing")
  println(art)

@main def runBook(): Unit = conversation:
  val topic = "functional programming"
  val Book(title, author, pages) = prompt[Book](s"Give me the best-selling book about $topic")
  println(s"The book $title is by $author and has $pages pages.")

@main def runMovie(): Unit = conversation:
  val Movie(title, genre, director) = prompt[Movie]("Inception movie genre and director.")
  println(s"The movie $title is a $genre film directed by $director.")

@main def runCities(): Unit = conversation:
  val cadiz = prompt[City]("Cádiz, Spain")
  val seattle = prompt[City]("Seattle, WA")
  println(s"The population of Cádiz is ${cadiz.population} and the population of Seattle is ${seattle.population}.")

@main def runRecipe(): Unit = conversation:
  val Recipe(name, ingredients) = prompt[Recipe]("Recipe for chocolate chip cookies.")
  println(s"The recipe for $name is $ingredients.")
