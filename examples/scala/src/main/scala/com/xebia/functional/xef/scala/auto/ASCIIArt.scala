package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class ASCIIArt(art: String) derives ScalaSerialDescriptor, Decoder

@main def runASCIIArt: Unit =
  lazy val asciiArt = ai {
    prompt[ASCIIArt]("ASCII art of a cat dancing")
  }
  println(asciiArt.art.getOrElse(ex => ASCIIArt("¯\\_(ツ)_/¯" + "\n" + ex.getMessage).art))
