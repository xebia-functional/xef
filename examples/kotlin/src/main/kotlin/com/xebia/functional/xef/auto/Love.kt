package com.xebia.functional.xef.auto

suspend fun main() {
  ai {
    val love: List<String> = promptMessage("tell me you like me with just emojis")
    println(love)
  }.getOrElse { println(it) }
}
