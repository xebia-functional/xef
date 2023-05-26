package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode

private final case class Book(name: String, author: String, summary: String) derives ScalaSerialDescriptor, Decoder

@main def runBook: Unit =
  val book = ai(prompt[Book]("To Kill a Mockingbird by Harper Lee summary."))
  println(s"To Kill a Mockingbird summary:\n ${book.summary}")
