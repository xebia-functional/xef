package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Colors(colors: List[String]) derives SerialDescriptor, Decoder

@main def runColors: Unit =
  ai {
    val colors = prompt[Colors]("A selection of 10 beautiful colors that go well together")
    println(colors)
  }.getOrElse(ex => println(ex.getMessage))
