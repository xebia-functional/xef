package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.log
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.tracing.Tracker
import com.xebia.functional.xef.tracing.createDispatcher
import kotlinx.serialization.Serializable

@Serializable data class Colors(val colors: List<String>)

suspend fun main() {
  OpenAI.conversation(createDispatcher(OpenAI.log, Tracker.Default)) {
    addContext("I want to paint my room")
    val colors: Colors = prompt("a selection of 10 beautiful colors that go well together")
    println(colors)
  }
}
