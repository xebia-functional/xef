package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.date.*

class LogsMetric(val logger: KLogger = KotlinLogging.logger {}) : Metric {

  private val indentSize = 4

  override suspend fun <A> promptSpan(
    conversation: Conversation,
    prompt: Prompt,
    block: suspend Metric.() -> A
  ): A {
    val millis = getTimeMillis()
    val name = prompt.messages.lastOrNull()?.content ?: "empty"
    logger.info { "Prompt-Span: $name" }
    logger.info {
      "${writeIndent()}|-- Conversation Id: ${conversation.conversationId?.value ?: "empty"}"
    }
    val output = block()
    logger.info { "${writeIndent()}|-- Finished in ${getTimeMillis()-millis} ms" }
    return output
  }

  override fun log(conversation: Conversation, message: String) {
    logger.info { "${writeIndent()}|-- $message" }
  }

  private fun writeIndent(times: Int = 1) = (1..indentSize * times).fold("") { a, b -> "$a " }
}
