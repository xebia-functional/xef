package com.xebia.functional.xef.metrics

import arrow.atomic.AtomicInt
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.date.*

class LogsMetric : Metric {

  private val numberOfBlocks = AtomicInt(0)

  private val indentSize = 4

  private val logger = KotlinLogging.logger {}

  override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A {
    val millis = getTimeMillis()
    logger.info { "${writeIndent(numberOfBlocks.get())}> Custom-Span: $name" }
    numberOfBlocks.incrementAndGet()
    val output = block()
    logger.info {
      "${writeIndent(numberOfBlocks.get())}|-- Finished in ${getTimeMillis() - millis} ms"
    }
    numberOfBlocks.decrementAndGet()
    return output
  }

  override suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A {
    val millis = getTimeMillis()
    val name = prompt.messages.lastOrNull()?.content ?: "empty"
    logger.info { "${writeIndent(numberOfBlocks.get())}> Prompt-Span: $name" }
    numberOfBlocks.incrementAndGet()
    val output = block()
    logger.info {
      "${writeIndent(numberOfBlocks.get())}|-- Finished in ${getTimeMillis() - millis} ms"
    }
    numberOfBlocks.decrementAndGet()
    return output
  }

  override fun event(message: String) {
    logger.info { "${writeIndent(numberOfBlocks.get())}|-- $message" }
  }

  override fun parameter(key: String, value: String) {
    logger.info { "${writeIndent(numberOfBlocks.get())}|-- $key = $value" }
  }

  private fun writeIndent(times: Int = 1) = (1..indentSize * times).fold("") { a, _ -> "$a " }
}
