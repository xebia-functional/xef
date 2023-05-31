package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Book(name: String, author: String, summary: String) derives ScalaSerialDescriptor, Decoder

@main def runBook: Unit =
  ai {
    val toKillAMockingBird = prompt[Book]("To Kill a Mockingbird by Harper Lee summary.")
    println(s"${toKillAMockingBird.name} by ${toKillAMockingBird.author} summary:\n ${toKillAMockingBird.summary}")
  }.getOrElse(ex => println(ex.getMessage))
