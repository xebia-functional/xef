package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai

suspend fun main() {
  ai {
    val love: List<String> = ai("tell me you like me with just emojis")
    println(love)
  }.getOrElse { println(it) }
}
