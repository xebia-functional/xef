package com.xebia.functional.xef.scala.auto

import com.xebia.functional.auto.*
import com.xebia.functional.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Book(name: String, author: String, summary: String) derives ScalaSerialDescriptor, Decoder

@main def runBook: Unit =
  val book = AI(prompt[Book]("To Kill a Mockingbird by Harper Lee summary."))
  println(s"To Kill a Mockingbird summary:\n ${book.summary}")
