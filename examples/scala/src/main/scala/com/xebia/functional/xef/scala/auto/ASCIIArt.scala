package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class ASCIIArt(art: String) derives ScalaSerialDescriptor, Decoder

@main def runASCIIArt: Unit =
  val asciiArt = ai(prompt[ASCIIArt]("ASCII art of a cat dancing"))
  println(asciiArt.art)
