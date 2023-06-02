package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class Person(name: String, age: Int) derives SerialDescriptor, Decoder

@main def runPerson: Unit =
  ai {
    val person = prompt[Person]("Hello made-up person, what is your name and age?")
    println(s"Hello ${person.name}, you are ${person.age} years old.")
  }.getOrElse(ex => println(ex.getMessage))
