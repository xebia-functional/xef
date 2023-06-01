package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Love(emojis: List[String]) derives ScalaSerialDescriptor, Decoder

@main def runLove: Unit =
  ai {
    val love = prompt[Love]("Tell me you like me with just emojis")
    println(love.emojis.mkString(""))
  }.getOrElse(ex => println(ex.getMessage))
