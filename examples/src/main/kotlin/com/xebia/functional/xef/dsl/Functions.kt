package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI

suspend fun add(x: Int, y : Int): Int =
  AI("Add $x + $y")

suspend fun main() {
  val result = add(1, 1)
  println(result) // 2
}
