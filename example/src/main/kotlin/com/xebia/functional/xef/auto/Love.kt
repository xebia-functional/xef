package com.xebia.functional.xef.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse

suspend fun main() {
  ai {
    val love: List<String> = promptMessage("tell me you like me with just emojis")
    println(love)
  }.getOrElse { println(it) }
}
