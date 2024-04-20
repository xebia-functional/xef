package com.xebia.functional.xef.llm.groq

import com.xebia.functional.xef.AI

suspend fun main() {
  val result = AI<String>("Add 1 + 1")
  println(result) // 2
}
