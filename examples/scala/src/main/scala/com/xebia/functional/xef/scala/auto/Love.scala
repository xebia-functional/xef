package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder
import io.circe.parser.decode

private final case class Love(emojis: List[String]) derives ScalaSerialDescriptor, Decoder

@main def runLove: Unit =
  val love = ai(prompt[Love]("Tell me you like me with just emojis"))
  println(love.emojis)
