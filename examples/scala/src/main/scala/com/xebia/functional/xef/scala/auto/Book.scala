package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Book(name: String, author: String, summary: String) derives ScalaSerialDescriptor, Decoder

def summarizeBook(title: String, author: String)(using scope: AIScope): Book =
  prompt(s"$title by $author summary.")

@main def runBook: Unit =
  ai {
    val toKillAMockingBird = summarizeBook("To Kill a Mockingbird", "Harper Lee")
    println(s"${toKillAMockingBird.name} by ${toKillAMockingBird.author} summary:\n ${toKillAMockingBird.summary}")
  }.getOrElse(ex => println(ex.getMessage))
