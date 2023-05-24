package com.xebia.functional.examples.auto

import cats.effect.{IO, IOApp}
import com.xebia.functional.auto.*
import com.xebia.functional.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

object Book extends IOApp.Simple:
  private final case class Book(name: String, author: String, summary: String) derives ScalaSerialDescriptor, Decoder

  def run: IO[Unit] =
    IO {
      val book = AI(prompt[Book]("To Kill a Mockingbird by Harper Lee summary."))
      println(s"To Kill a Mockingbird summary:\n ${book.summary}")
    }
