package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.scala.auto.ScalaSerialDescriptorContext.given
import io.circe.Decoder

private final case class Person(name: String, age: Int) derives ScalaSerialDescriptor, Decoder

@main def runPerson: Unit =
  val person = ai(prompt[Person]("What is your name and age?"))
  println("Hello ${person.name}, you are ${person.age} years old.")
