package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.auto.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  OpenAI.conversation {
    val love: String = prompt(Prompt("tell me you like me with just emojis"))
    println(love)
  }
}
