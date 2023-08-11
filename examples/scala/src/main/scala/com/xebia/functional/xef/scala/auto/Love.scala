package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Love(emojis: List[String]) derives SerialDescriptor, Decoder

@main def runLove: Unit =
  conversation {
    val love = prompt[Love]("Tell me you like me with just emojis")
    println(love.emojis.mkString(""))
  }
