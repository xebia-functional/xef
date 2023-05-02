package com.xebia.functional.auto

suspend fun main() {
  val love: List<String> = ai("tell me you like me with just emojis")
  println(love)
}
