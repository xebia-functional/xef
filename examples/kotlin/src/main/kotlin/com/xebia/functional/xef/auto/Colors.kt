package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Colors(val colors: List<String>)

suspend fun main() {
  OpenAI.conversation {
    val colors: Colors = prompt("a selection of 10 beautiful colors that go well together")
    println(colors)
  }
}
