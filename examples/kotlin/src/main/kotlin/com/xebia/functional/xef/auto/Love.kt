package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.promptMessage

suspend fun main() {
  OpenAI.conversation {
    val love: String = promptMessage("tell me you like me with just emojis")
    println(love)
  }
}
