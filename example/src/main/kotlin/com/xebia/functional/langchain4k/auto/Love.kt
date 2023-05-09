package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.prompt
import com.xebia.functional.auto.getOrElse

suspend fun main() {
  prompt {
    val love: List<String> = prompt("tell me you like me with just emojis")
    println(love)
  }.getOrElse { println(it) }
}
