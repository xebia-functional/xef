package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.date.*

class LogsMetric : Metric {

  private val logger: KLogger = KotlinLogging.logger {}

  override suspend fun <A> promptSpan(
    conversation: Conversation,
    prompt: Prompt,
    block: suspend Metric.() -> A
  ): A {
    val milis = getTimeMillis()
    val name = "Prompt: ${prompt.messages.lastOrNull()?.content ?: "empty"}"
    logger.info { "Start span: $name" }
    val output = block()
    logger.info { "End span (${getTimeMillis()-milis} ms): $name" }
    return output
  }

  override fun log(conversation: Conversation, message: String) {
    logger.info { message }
  }
}
