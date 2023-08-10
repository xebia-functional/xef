package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class ASCIIArt(art: String) derives SerialDescriptor, Decoder

@main def runASCIIArt: Unit =
  lazy val asciiArt = conversation {
    prompt[ASCIIArt]("ASCII art of a cat dancing")
  }
  println(asciiArt.art)
