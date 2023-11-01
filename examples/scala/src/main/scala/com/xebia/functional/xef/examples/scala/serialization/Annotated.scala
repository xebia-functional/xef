package com.xebia.functional.xef.examples.scala.serialization

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

@Description("A book")
case class AnnotatedBook(
    @Description("The name of the book") name: String,
    @Description("The author of the book") author: String,
    @Description("A 50 word paragraph with a summary of this book") summary: String
) derives SerialDescriptor,
      Decoder

@main def runAnnotatedBook(): Unit = conversation:
  val book = prompt[AnnotatedBook]("To Kill a Mockingbird")
  println(book)
