package com.xebia.functional.xef.auto.memory

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.auto.llm.openai.promptMessage

suspend fun main() {
  ai {
    val hello = "Hello, my name is Jane"
    println(hello)
    val aiResponse = promptMessage(hello)
    println("AI: $aiResponse")
    val whatIsMyName = "What is my name?"
    println(whatIsMyName)
    val aiSecondResponse = promptMessage(whatIsMyName)
    println("AI: $aiSecondResponse")
  }.getOrThrow()
}
