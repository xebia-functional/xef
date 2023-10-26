package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import io.ktor.util.date.*

class LogsMetric : Metric {

  private val indentSize = 4

  override suspend fun <A> promptSpan(
    conversation: Conversation,
    prompt: Prompt,
    block: suspend Metric.() -> A
  ): A {
    val millis = getTimeMillis()
    val name = prompt.messages.lastOrNull()?.content ?: "empty"
    println("Prompt-Span: $name")
    println("${writeIndent()}|-- Conversation Id: ${conversation.conversationId?.value ?: "empty"}")
    val output = block()
    println("${writeIndent()}|-- Finished in ${getTimeMillis() - millis} ms")
    return output
  }

  override fun log(conversation: Conversation, message: String) {
    println("${writeIndent()}|-- $message")
  }

  private fun writeIndent(times: Int = 1) = (1..indentSize * times).fold("") { a, b -> "$a " }
}
