package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI

suspend fun main() {
  val two: Int = AI("What is 1 + 1?")
  val truth: Boolean = AI("Is the earth flat?")
  val name: String = AI("Hi AI, What is your name?")
  // val many : List<String> = AI("What are the names of the planets in the solar system?")
  // val map : Map<String, String> = AI("What are the names and brief description of the planets in
  // the solar system?")
  println(
    """
    |two: $two
    |truth: $truth
    |name: $name
  """
      .trimMargin()
  )
}
