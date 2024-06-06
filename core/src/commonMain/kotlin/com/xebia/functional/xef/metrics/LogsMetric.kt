package com.xebia.functional.xef.metrics

import arrow.atomic.AtomicInt
import com.xebia.functional.openai.generated.model.MessageObject
import com.xebia.functional.openai.generated.model.RunObject
import com.xebia.functional.openai.generated.model.RunStepObject
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.contentAsString
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.Level
import io.ktor.util.date.*

class LogsMetric(private val level: Level = Level.INFO) : Metric {

  private val numberOfBlocks = AtomicInt(0)

  private val indentSize = 4

  private val logger = KotlinLogging.logger {}

  override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A {
    val millis = getTimeMillis()
    logger.at(level) { message = "${writeIndent(numberOfBlocks.get())}> Custom-Span: $name" }
    numberOfBlocks.incrementAndGet()
    val output = block()
    logger.at(level) {
      message = "${writeIndent(numberOfBlocks.get())}|-- Finished in ${getTimeMillis() - millis} ms"
    }
    numberOfBlocks.decrementAndGet()
    return output
  }

  override suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A {
    val millis = getTimeMillis()
    val name = prompt.messages.lastOrNull()?.contentAsString() ?: "empty"
    logger.at(level) { message = "${writeIndent(numberOfBlocks.get())}> Prompt-Span: $name" }
    numberOfBlocks.incrementAndGet()
    val output = block()
    logger.at(level) {
      message = "${writeIndent(numberOfBlocks.get())}|-- Finished in ${getTimeMillis() - millis} ms"
    }
    numberOfBlocks.decrementAndGet()
    return output
  }

  override suspend fun assistantCreateRun(runObject: RunObject, source: String) {
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- AssistantId: ${runObject.assistantId}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- ThreadId: ${runObject.threadId}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- RunId: ${runObject.id}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- Status: ${runObject.status.name}"
    }
  }

  override suspend fun assistantCreateRunStep(runObject: RunStepObject, source: String) {
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- AssistantId: ${runObject.assistantId}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- ThreadId: ${runObject.threadId}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- RunId: ${runObject.id}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- Status: ${runObject.status.name}"
    }
  }

  override suspend fun assistantCreatedMessage(messageObject: MessageObject, source: String) {
    logger.at(level) {
      this.message =
        "${writeIndent(numberOfBlocks.get())}|-- AssistantId: ${messageObject.assistantId}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- ThreadId: ${messageObject.threadId}"
    }
    logger.at(level) {
      this.message = "${writeIndent(numberOfBlocks.get())}|-- RunId: ${messageObject.id}"
    }
    logger.at(level) {
      if (messageObject.status != null) {
        this.message =
          "${writeIndent(numberOfBlocks.get())}|-- Status: ${messageObject.status!!.name}"
      }
    }
  }

  override suspend fun event(message: String) {
    logger.at(level) { this.message = "${writeIndent(numberOfBlocks.get())}|-- $message" }
  }

  override suspend fun parameter(key: String, value: String) {
    logger.at(level) { message = "${writeIndent(numberOfBlocks.get())}|-- $key = $value" }
  }

  override suspend fun parameter(key: String, values: List<String>) {
    logger.at(level) { message = "${writeIndent(numberOfBlocks.get())}|-- $key = $values" }
  }

  private fun writeIndent(times: Int = 1) = (1..indentSize * times).fold("") { a, b -> "$a " }
}
