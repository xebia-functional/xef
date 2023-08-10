package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.promptMessage

suspend fun main() {
  conversation {
    val love: String = promptMessage("tell me you like me with just emojis")
    println(love)
  }.getOrElse { println(it) }
}
