package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI

suspend fun main() {
  val two: Int = AI("What is 1 + 1?")
  val truth: Boolean = AI("Is the earth flat?")
  val name: String = AI("Hi AI, What is your name?")
  println(
    """
    |two: $two
    |truth: $truth
    |name: $name
  """
      .trimMargin()
  )
}
