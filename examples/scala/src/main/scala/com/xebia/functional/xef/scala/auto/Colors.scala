package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Colors(colors: List[String]) derives ScalaSerialDescriptor, Decoder

@main def runColors: Unit =
  val colors = ai(prompt[Colors]("A selection of 10 beautiful colors that go well together"))
  println(colors)
