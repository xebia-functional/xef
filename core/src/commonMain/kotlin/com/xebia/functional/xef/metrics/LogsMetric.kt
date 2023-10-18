package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import io.ktor.util.date.*

class LogsMetric : Metric {

  private val identSize = 4

  override suspend fun <A> promptSpan(
    conversation: Conversation,
    prompt: Prompt,
    block: suspend Metric.() -> A
  ): A {
    val milis = getTimeMillis()
    val name = prompt.messages.lastOrNull()?.content ?: "empty"
    println("Prompt-Span: $name")
    val output = block()
    println("${writeIdent()}|-- Finished in ${getTimeMillis()-milis} ms")
    return output
  }

  override fun log(conversation: Conversation, message: String) {
    println("${writeIdent()}|-- $message".padStart(identSize, ' '))
  }

  private fun writeIdent(times: Int = 1) = (1..identSize * times).fold("") { a, b -> "$a " }
}
